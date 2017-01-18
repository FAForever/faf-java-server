package com.faforever.server.security;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.HardwareInformation;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.UniqueIdExempt;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.RequestExceptionWithCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.google.common.primitives.Bytes;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.openssl.PEMReader;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UniqueIdServiceTest {

  private static final String PRIVATE_KEY = "MIGuAgEAMA0GCSqGSIb3DQEBAQUABIGZMIGWAgEAAhxKgmLjzUModkCeLHUBD35J" +
    "VT19nr7oC8ABnrpzAgMBAAECHEDSFCQos89kqSyycowbtRaID4S4krdSQ7PSSjEC" +
    "DwCN4VaucAaHTvKoY5MF2wIPAIZwp65APMeoO+q9LN1JAg4kRLM02jxBAIR1WSbv" +
    "OwIOX9d2IupWqjuMI4ejsUkCDma6Pvo7IyhgG07lQDX9";

  private static final String PUBLIC_KEY = "MDcwDQYJKoZIhvcNAQEBBQADJgAwIwIcSoJi481DKHZAnix1AQ9+SVU9fZ6+6AvA" +
    "AZ66cwIDAQAB";

  private static final String JSON = "{\n" +
    "  \"session\": \"\",\n" +
    "  \"desktop\": {\n" +
    "    \"width\": 1080,\n" +
    "    \"height\": 1920\n" +
    "  },\n" +
    "  \"machine\": {\n" +
    "    \"motherboard\": {\n" +
    "      \"vendor\": \"Gigabyte Technology Co., Ltd.\",\n" +
    "      \"name\": \"Z68XP-UD4\"\n" +
    "    },\n" +
    "    \"model\": \"Z68XP-UD4\",\n" +
    "    \"processor\": {\n" +
    "      \"name\": \"Intel(R) Core(TM) i7-2600K CPU @ 3.40GHz\",\n" +
    "      \"id\": \"BFEBFBFF000306A7\"\n" +
    "    },\n" +
    "    \"memory\": {\n" +
    "      \"serial0\": null\n" +
    "    },\n" +
    "    \"uuid\": \"00000000-0000-0000-0000-1C7F65F99C54\",\n" +
    "    \"os\": {\n" +
    "      \"version\": \"10.0.10586\",\n" +
    "      \"type\": \"Award Software International, Inc.\"\n" +
    "    },\n" +
    "    \"bios\": {\n" +
    "      \"manufacturer\": null,\n" +
    "      \"version\": \"GBT    - 42302e31\",\n" +
    "      \"date\": \"20110620000000.000000+000\",\n" +
    "      \"serial\": null,\n" +
    "      \"description\": \"Award Modular BIOS v6.00PG\",\n" +
    "      \"smbbversion\": \"F1\"\n" +
    "    },\n" +
    "    \"disks\": {\n" +
    "      \"vserial\": \"EE17B56E\",\n" +
    "      \"controller_id\": \"PCI\\\\VEN_8086&DEV_1C00&SUBSYS_B0021458&REV_05\\\\3&14C0B0C5&0&FA\"\n" +
    "    }\n" +
    "  }\n" +
    "}";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private UniqueIdService instance;
  private ServerProperties properties;

  @Mock
  private HardwareInformationRepository hardwareInformationRepository;

  private Player player;
  private HardwareInformation hardwareInformation;

  @Before
  public void setUp() throws Exception {
    player = newPlayer(51234);
    properties = new ServerProperties();
    properties.getUid().setEnabled(true);
    properties.getUid().setPrivateKey(PRIVATE_KEY);

    hardwareInformation = new HardwareInformation();
    hardwareInformation.setPlayers(new HashSet<>());

    when(hardwareInformationRepository.save(any(HardwareInformation.class)))
      .thenAnswer(invocation -> invocation.getArgumentAt(0, HardwareInformation.class));

    instance = new UniqueIdService(properties, new ObjectMapper(), hardwareInformationRepository);
  }

  @Test
  public void verifyCreatesNewHardwareInformation() throws Exception {
    assertThat(hardwareInformation.getPlayers(), hasSize(0));
    when(hardwareInformationRepository.findOneByHash(anyString())).thenReturn(Optional.empty());

    instance.verify(player, toUid(JSON));

    ArgumentCaptor<HardwareInformation> captor = ArgumentCaptor.forClass(HardwareInformation.class);
    verify(hardwareInformationRepository).save(captor.capture());

    HardwareInformation hardwareInformation = captor.getValue();
    assertThat(hardwareInformation.getPlayers(), hasSize(1));
    assertThat(hardwareInformation.getPlayers().iterator().next(), is(player));
    assertThat(hardwareInformation.getDeviceId(), is("PCI\\VEN_8086&DEV_1C00&SUBSYS_B0021458&REV_05\\3&14C0B0C5&0&FA"));
    assertThat(hardwareInformation.getManufacturer(), is(nullValue()));
    assertThat(hardwareInformation.getMemSerialNumber(), is(nullValue()));
    assertThat(hardwareInformation.getName(), is("Intel(R) Core(TM) i7-2600K CPU @ 3.40GHz"));
    assertThat(hardwareInformation.getProcessorId(), is("BFEBFBFF000306A7"));
    assertThat(hardwareInformation.getSerialNumber(), is(nullValue()));
    assertThat(hardwareInformation.getSmbiosbiosVersion(), is("F1"));
    assertThat(hardwareInformation.getUuid(), is("00000000-0000-0000-0000-1C7F65F99C54"));
    assertThat(hardwareInformation.getVolumeSerialNumber(), is("EE17B56E"));
    assertThat(hardwareInformation.getHash(), is("a41c191809f33ce0d447ffe74d443290"));

    verify(hardwareInformationRepository).save(hardwareInformation);
  }

  @Test
  public void verifyAddsPlayerToExistingHardwareInformation() throws Exception {
    assertThat(hardwareInformation.getPlayers(), hasSize(0));
    when(hardwareInformationRepository.findOneByHash(anyString())).thenReturn(Optional.of(hardwareInformation));

    instance.verify(player, toUid(JSON));

    verify(hardwareInformationRepository).save(hardwareInformation);
    assertThat(hardwareInformation.getPlayers(), hasSize(1));
    assertThat(hardwareInformation.getPlayers().iterator().next(), is(player));
  }

  @Test
  public void verifySkipsIfDisabled() throws Exception {
    properties.getUid().setEnabled(false);
    instance = new UniqueIdService(properties, null, null);

    instance.verify(player, toUid(JSON));

    verifyZeroInteractions(hardwareInformationRepository);
  }

  @Test
  public void verifySkipsIfExemptAvailable() throws Exception {
    player.setUniqueIdExempt(new UniqueIdExempt());

    instance.verify(player, toUid(JSON));

    verifyZeroInteractions(hardwareInformationRepository);
  }

  @Test
  public void verifySkipsIfSteamIdAvailable() throws Exception {
    player.setSteamId("123");

    instance.verify(player, toUid(JSON));

    verifyZeroInteractions(hardwareInformationRepository);
  }

  @Test
  public void verifyUniqueIdAlreadyInUseByAnother() throws Exception {
    assertThat(hardwareInformation.getPlayers(), hasSize(0));
    when(hardwareInformationRepository.findOneByHash(anyString())).thenReturn(Optional.of(hardwareInformation));

    hardwareInformation.setPlayers(Sets.newHashSet(newPlayer(1)));

    expectedException.expect(RequestExceptionWithCode.requestExceptionWithCode(ErrorCode.UID_USED_BY_ANOTHER_USER));
    instance.verify(player, toUid(JSON));
  }

  @Test
  public void verifyUniqueIdAlreadyInUseByMultipleUsers() throws Exception {
    assertThat(hardwareInformation.getPlayers(), hasSize(0));
    hardwareInformation.setPlayers(Sets.newHashSet(newPlayer(1), newPlayer(2)));
    when(hardwareInformationRepository.findOneByHash(anyString())).thenReturn(Optional.of(hardwareInformation));

    expectedException.expect(RequestExceptionWithCode.requestExceptionWithCode(ErrorCode.UID_USED_BY_MULTIPLE_USERS));
    instance.verify(player, toUid(JSON));
  }

  @Test
  public void verifyHardwareInformationExistsForThisUser() throws Exception {
    assertThat(hardwareInformation.getPlayers(), hasSize(0));
    when(hardwareInformationRepository.findOneByHash(anyString())).thenReturn(Optional.of(hardwareInformation));

    hardwareInformation.setPlayers(Sets.newHashSet(player));

    instance.verify(player, toUid(JSON));

    verify(hardwareInformationRepository, never()).save(any(HardwareInformation.class));
  }

  /**
   * This implements the client-side, legacy UID generation.
   */
  private static String toUid(String string) throws IOException, NoSuchAlgorithmException, InvalidCipherTextException {
    // Step 1: Load public key
    PublicKey publicKey = loadPublicKey();

    // Step 2: Generate 16 bytes initialization vector, encode base 64
    byte[] initVector = new byte[16];
    new SecureRandom().nextBytes(initVector);
    byte[] base64InitVector = Base64.getEncoder().encode(initVector);

    // Step 3: Generate AES key 16 bytes, encrypt it, encode base 64
    KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
    aesKeyGen.init(128);
    SecretKey aesKey = aesKeyGen.generateKey();
    byte[] rsaEncryptedAesKey = rsaEncrypt(aesKey.getEncoded(), PublicKeyFactory.createKey(publicKey.getEncoded()));
    byte[] rsaEncryptedBsa64EncodedAesKey = Base64.getEncoder().encode(rsaEncryptedAesKey);

    // Step 4: AES-encrypt the json string, encode base 64
    // Prefix the JSON string with char '2' to indicate the new uid data format for the server
    String jsonString = "2" + string;

    // Insert trailing bytes to make len(json_string) a multiple of 16
    int jsonLength = jsonString.length();
    int trailLength = ((((jsonLength / 16) + 1) * 16) - jsonLength);
    // Because someone thought manual padding would be necessary
    jsonString = jsonString + new String(new char[trailLength]).replace("\0", "x");

    byte[] encryptedString = aesEncrypt(initVector, jsonString.getBytes(UTF_8), aesKey.getEncoded());
    byte[] encryptedStringBase64 = Base64.getEncoder().encode(encryptedString);

    // Step 5: put message together
    byte[] messageBytes = Bytes.concat(new byte[]{(byte) trailLength}, base64InitVector, encryptedStringBase64, rsaEncryptedBsa64EncodedAesKey);
    return Base64.getEncoder().encodeToString(messageBytes);
  }

  private static PublicKey loadPublicKey() throws IOException {
    try (PEMReader pemReader = new PEMReader(new StringReader("-----BEGIN PUBLIC KEY-----\n" + PUBLIC_KEY + "\n-----END PUBLIC KEY-----"))) {
      return (PublicKey) pemReader.readObject();
    }
  }

  private static byte[] aesEncrypt(byte[] initVector, byte[] payload, byte[] aesKey) throws InvalidCipherTextException {
    PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
    cipher.init(true, new ParametersWithIV(new KeyParameter(aesKey), initVector));

    byte[] outBuf = new byte[cipher.getOutputSize(payload.length)];
    int length = cipher.processBytes(payload, 0, payload.length, outBuf, 0);
    length += cipher.doFinal(outBuf, length);
    byte[] result = new byte[length];
    System.arraycopy(outBuf, 0, result, 0, result.length);
    return result;
  }

  private static byte[] rsaEncrypt(byte[] encryptedData, CipherParameters privateKey) throws IOException, InvalidCipherTextException {
    AsymmetricBlockCipher engine = new PKCS1Encoding(new RSAEngine());
    engine.init(true, privateKey);
    return engine.processBlock(encryptedData, 0, encryptedData.length);
  }

  @NotNull
  private static Player newPlayer(int id) {
    Player player = new Player();
    player.setId(id);
    return player;
  }
}
