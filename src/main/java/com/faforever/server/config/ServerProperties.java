package com.faforever.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "faf-server", ignoreUnknownFields = false)
public class ServerProperties {

  private int port = 8001;
  private String version = "dev";
  private String apiBaseUrl = "http://localhost:8080";
  private TrueSkill trueSkill = new TrueSkill();
  private Uid uid = new Uid();

  @Data
  public static class TrueSkill {
    private double initialMean;
    private double initialStandardDeviation;
    private double beta;
    private double dynamicFactor;
    private double drawProbability;
  }

  @Data
  public static class Uid {
    private boolean enabled;
    /**
     * PKCS#8 private key without decoration and newlines. Use the following command to convert from PKCS#1:
     * <p>
     * <code>openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in pkcs1.key -out pkcs8.key</code>
     * </p>
     * Note that the key currently has to be a 244 bit key.
     */
    private String privateKey;
    private String linkToSteamUrl;
  }
}
