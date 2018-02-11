package com.faforever.server.config;

import com.faforever.server.api.JsonApiMessageConverter;
import com.faforever.server.config.ServerProperties.Api;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/** Configuration for FAF-API access. */
@Configuration
public class ApiConfig {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, JsonApiMessageConverter jsonApiMessageConverter, ServerProperties properties) throws IOException {
    Api api = properties.getApi();

    ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
    resourceDetails.setClientId(api.getClientId());
    resourceDetails.setClientSecret(api.getClientSecret());
    resourceDetails.setAccessTokenUri(api.getAccessTokenUri());

    return restTemplateBuilder
      .additionalMessageConverters(jsonApiMessageConverter)
      .rootUri(api.getBaseUrl())
      .configure(new OAuth2RestTemplate(resourceDetails));
  }
}
