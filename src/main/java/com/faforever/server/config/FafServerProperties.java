package com.faforever.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "faf-server", ignoreUnknownFields = false)
public class FafServerProperties {

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
     * Base64-DER-encoded private key.
     */
    private String privateKey;
    private String linkToSteamUrl;
  }
}
