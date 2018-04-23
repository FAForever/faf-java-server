package com.faforever.server.config.security;

import com.faforever.server.config.security.NoopWebSecurityConfigurerAdapter.TestingPasswordEncoder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NoopWebSecurityConfigurerAdapterTest {

  private NoopWebSecurityConfigurerAdapter instance;

  @Mock
  private UserDetailsService userDetailsService;

  @Before
  public void setUp() throws Exception {
    instance = new NoopWebSecurityConfigurerAdapter(userDetailsService);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test() throws Exception {
    AuthenticationManagerBuilder auth = Mockito.mock(AuthenticationManagerBuilder.class);
    DaoAuthenticationConfigurer<AuthenticationManagerBuilder, UserDetailsService> configurer = Mockito.mock(DaoAuthenticationConfigurer.class);
    when(auth.userDetailsService(userDetailsService)).thenReturn(configurer);

    instance.configure(auth);

    verify(auth).userDetailsService(userDetailsService);
    verify(configurer).passwordEncoder(TestingPasswordEncoder.INSTANCE);
  }

  @Test
  public void passwordEncoderEncode() {
    assertThat(TestingPasswordEncoder.INSTANCE.encode("foo"), is("foo"));
  }

  @Test
  public void passwordEncoderMatches() {
    assertThat(TestingPasswordEncoder.INSTANCE.matches("foo", "bar"), is(true));
  }
}
