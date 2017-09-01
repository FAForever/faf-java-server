package com.faforever.server.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import javax.inject.Inject;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends GlobalMethodSecurityConfiguration {

  private final UserDetailsService userDetailsService;

  @Inject
  public SecurityConfig(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth
      .userDetailsService(userDetailsService)
      .passwordEncoder(NoOpPasswordEncoder.getInstance());
  }
}
