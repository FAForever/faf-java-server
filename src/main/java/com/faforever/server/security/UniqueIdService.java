package com.faforever.server.security;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.HardwareInformation;
import com.faforever.server.entity.Player;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.Requests;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Objects;
import java.util.Set;

import static com.github.nocatch.NoCatch.noCatch;
import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Slf4j
@Transactional
public class UniqueIdService {
  private final ObjectMapper objectMapper;
  private final boolean enabled;
  private final HardwareInformationRepository hardwareInformationRepository;
  private final String linkToSteamUrl;
  private AsymmetricKeyParameter privateKey;

  @Inject
  public UniqueIdService(ServerProperties properties, ObjectMapper objectMapper, HardwareInformationRepository hardwareInformationRepository) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
    this.objectMapper = objectMapper;
    this.enabled = properties.getUid().isEnabled();
    this.linkToSteamUrl = properties.getUid().getLinkToSteamUrl();
    this.hardwareInformationRepository = hardwareInformationRepository;

    if (enabled) {
      Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
      privateKey = PrivateKeyFactory.createKey(Base64.getDecoder().decode(properties.getUid().getPrivateKey().getBytes(UTF_8)));
    }
  }

  public void verify(Player player, String uid) {
    if (!enabled) {
      log.debug("Skipping unique ID check for player '{}' because it is disabled", player);
      return;
    }

    if (player.getUniqueIdExempt() != null) {
      log.debug("Skipping unique ID check for player '{}' because: {}", player, player.getUniqueIdExempt().getReason());
      return;
    }

    if (player.getSteamId() != null) {
      log.debug("Skipping unique ID check for player '{}' because of steam ID: {}", player, player.getSteamId());
      return;
    }

    UidPayload uidPayload = noCatch(() -> extractPayload(uid));
    String hash = Hashing.md5().hashString(
      uidPayload.getMachine().getUuid()
        + uidPayload.getMachine().getMemory().getSerial0()
        + uidPayload.getMachine().getDisks().getControllerId()
        + uidPayload.getMachine().getBios().getManufacturer()
        + uidPayload.getMachine().getProcessor().getName()
        + uidPayload.getMachine().getProcessor().getId()
        + uidPayload.getMachine().getBios().getSmbbVersion()
        + uidPayload.getMachine().getBios().getSerial()
        + uidPayload.getMachine().getDisks().getVSerial()
      , UTF_8
    ).toString();

    HardwareInformation information = hardwareInformationRepository.findOneByHash(hash)
      .orElseGet(() -> hardwareInformationRepository.save(new HardwareInformation(0,
        hash,
        uidPayload.getMachine().getUuid(),
        uidPayload.getMachine().getMemory().getSerial0(),
        uidPayload.getMachine().getDisks().getControllerId(),
        uidPayload.getMachine().getBios().getManufacturer(),
        uidPayload.getMachine().getProcessor().getName(),
        uidPayload.getMachine().getProcessor().getId(),
        uidPayload.getMachine().getBios().getSmbbVersion(),
        uidPayload.getMachine().getBios().getSerial(),
        uidPayload.getMachine().getDisks().getVSerial(),
        Sets.newHashSet(player)
      )));

    Set<Player> players = information.getPlayers();
    int count = players.size();

    Requests.verify(count < 2, ErrorCode.UID_USED_BY_MULTIPLE_USERS, linkToSteamUrl);
    Requests.verify(count == 0 || Objects.equals(players.iterator().next().getId(), player.getId()), ErrorCode.UID_USED_BY_ANOTHER_USER, linkToSteamUrl);

    if (count == 0) {
      // This happens if hardware information is already present but the user associations have been deleted.
      information.getPlayers().add(player);
      hardwareInformationRepository.save(information);
    }
    log.debug("Player '{}' passed unique ID check", player);
  }

  private UidPayload extractPayload(String uid) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, InvalidAlgorithmParameterException, InvalidCipherTextException {
    Decoder b64Decoder = Base64.getDecoder();

    byte[] bytes = b64Decoder.decode(uid.getBytes(UTF_8));

    // Because someone thought manual padding would be necessary
    int trailLength = bytes[0];
    byte[] initVector = b64Decoder.decode(Arrays.copyOfRange(bytes, 1, 25));
    byte[] aesEncryptedJson = b64Decoder.decode(Arrays.copyOfRange(bytes, 25, bytes.length - 40));
    byte[] rsaEncryptedAesKey = b64Decoder.decode(Arrays.copyOfRange(bytes, bytes.length - 40, bytes.length));

    // The JSON string is AES encrypted. Decrypt the AES key with our RSA key.
    byte[] aesKey = rsaDecrypt(rsaEncryptedAesKey);

    // Then decrypt the AES encrypted message
    byte[] plaintext = aesDecrypt(initVector, aesEncryptedJson, aesKey);

    // The JSON string is prefixed with the magic byte "2", meaning version 2 of the UID's JSON
    String json = new String(plaintext, 1, plaintext.length - trailLength, UTF_8);
    return objectMapper.readValue(json, UidPayload.class);
  }

  private byte[] aesDecrypt(byte[] initVector, byte[] aesEncryptedJson, byte[] aesKey) throws InvalidCipherTextException {
    PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
    cipher.init(false, new ParametersWithIV(new KeyParameter(aesKey), initVector));

    int plaintextSize = cipher.processBytes(aesEncryptedJson, 0, aesEncryptedJson.length, aesEncryptedJson, 0);
    plaintextSize += cipher.doFinal(aesEncryptedJson, plaintextSize);
    return Arrays.copyOf(aesEncryptedJson, plaintextSize);
  }

  private byte[] rsaDecrypt(byte[] encryptedData) throws IOException, InvalidCipherTextException {
    AsymmetricBlockCipher engine = new PKCS1Encoding(new RSAEngine());
    engine.init(false, privateKey);
    return engine.processBlock(encryptedData, 0, encryptedData.length);
  }
}
