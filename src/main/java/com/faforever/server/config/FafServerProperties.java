package com.faforever.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "faf-server", ignoreUnknownFields = false)
public class FafServerProperties {

  private int port = 8001;
  private String version = "dev";
  private String apiBaseUrl = "http://localhost:8080";
}
