package com.faforever.server.config;

import com.faforever.server.security.BanDetailsService;
import com.faforever.server.security.PolicyService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PolicyServiceConfig {
  @Bean
  public PolicyService policyService(ServerProperties properties, BanDetailsService banDetailsService) {
    RestTemplate restTemplate = new RestTemplateBuilder()
      .rootUri(properties.getPolicyService().getUrl())
      .build();
    return new PolicyService(properties, banDetailsService, restTemplate);
  }
}
