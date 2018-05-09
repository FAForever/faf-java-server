package com.faforever.server.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.security.channel.ChannelSecurityInterceptor;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;

import java.util.Collections;

@Configuration
public class IntegrationSecurityConfig {

  @Bean
  public SecurityContextChannelInterceptor securityContextChannelInterceptor() {
    return new SecurityContextChannelInterceptor();
  }

  @Bean
  public ChannelSecurityInterceptor channelSecurityInterceptor(AuthenticationManager authenticationManager) {
    ChannelSecurityInterceptor channelSecurityInterceptor = new ChannelSecurityInterceptor();
    channelSecurityInterceptor.setAuthenticationManager(authenticationManager);
    channelSecurityInterceptor.setAccessDecisionManager(new AffirmativeBased(Collections.singletonList(
      new RoleVoter()
    )));
    return channelSecurityInterceptor;
  }
}
