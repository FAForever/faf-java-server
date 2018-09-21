package com.faforever.server.config.security;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

@Configuration
@ConditionalOnProperty(value = "faf-server.disable-authentication", matchIfMissing = true, havingValue = "false")
public class FafWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

  private final UserDetailsService userDetailsService;

  public FafWebSecurityConfigurerAdapter(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
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
  }
}
