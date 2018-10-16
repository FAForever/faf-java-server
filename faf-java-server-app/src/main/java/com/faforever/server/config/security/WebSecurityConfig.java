package com.faforever.server.config.security;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final Optional<AuthenticationProvider> authenticationProvider;
  private final UserDetailsService userDetailsService;

  public WebSecurityConfig(Optional<AuthenticationProvider> authenticationProvider, UserDetailsService userDetailsService) {
    this.authenticationProvider = authenticationProvider;
    this.userDetailsService = userDetailsService;
  }

  private ObjectPostProcessor<DaoAuthenticationProvider> daoAuthenticationProviderPostProcessor() {
    return new ObjectPostProcessor<>() {
      @Override
      public <O extends DaoAuthenticationProvider> O postProcess(O object) {
        object.setPreAuthenticationChecks(new FafAccountBannedChecker());
        return object;
      }
    };
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    String idForEncode = "bcrypt";
    Map<String, PasswordEncoder> encoders = ImmutableMap.of(
      idForEncode, new BCryptPasswordEncoder()
    );

    DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder(idForEncode, encoders);
    passwordEncoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());

    auth
      .userDetailsService(userDetailsService)
      .withObjectPostProcessor(daoAuthenticationProviderPostProcessor())
      .passwordEncoder(passwordEncoder);

    authenticationProvider.ifPresent(auth::authenticationProvider);
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .authorizeRequests()
      .requestMatchers(EndpointRequest.toAnyEndpoint())
      .permitAll()
      .anyRequest().authenticated()
      .and().formLogin().and().httpBasic()
    ;
  }
}
