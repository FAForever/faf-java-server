package com.faforever.server.security;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.springframework.util.Assert;
import sun.security.rsa.RSAPrivateCrtKeyImpl;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Base64;

@UtilityClass
final class RsaHelper {

  /**
   * Reads a specified PKCS#1 formatted key (without any headers or footers). Having the key in PKCS#8 format would be
   * easier as bouncy castle provides a one-liner to read it but since the original FAF server had its key in PKCS#1,
   * this method allows to just use the same key string instead of having to convert it.
   */
  @SneakyThrows
  RSAPrivateCrtKeyImpl readPkcs1(String content) {
    ASN1Sequence seq = ASN1Sequence.getInstance(Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8)));
    Assert.notNull(seq, "RSA private key has not been specified properly. Value is '" + content + "'.");
    Assert.isTrue(seq.size() == 9, "Invalid RSA Private Key ASN1 sequence.");

    RSAPrivateKey key = RSAPrivateKey.getInstance(seq);
    RSAPrivateCrtKeySpec privSpec = new RSAPrivateCrtKeySpec(
      key.getModulus(),
      key.getPublicExponent(),
      key.getPrivateExponent(),
      key.getPrime1(),
      key.getPrime2(),
      key.getExponent1(),
      key.getExponent2(),
      key.getCoefficient()
    );

    return (RSAPrivateCrtKeyImpl) KeyFactory.getInstance("RSA").generatePrivate(privSpec);
  }
}
