package com.faforever.server.config.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Does not check user passwords. Not for production use, only for testing environments.
 */
@Configuration
@ConditionalOnProperty(value = "faf-server.disable-authentication")
public class NoopWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

  private final UserDetailsService userDetailsService;

  public NoopWebSecurityConfigurerAdapter(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  enum TestingPasswordEncoder implements PasswordEncoder {
    INSTANCE;

    @Override
    public String encode(CharSequence rawPassword) {
      return String.valueOf(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
      return true;
    }
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth
      .userDetailsService(userDetailsService)
      .passwordEncoder(TestingPasswordEncoder.INSTANCE);
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }
}
