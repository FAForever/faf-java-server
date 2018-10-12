package com.faforever.server.config.security;

import com.faforever.server.config.ServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static org.springframework.security.oauth2.common.OAuth2AccessToken.ACCESS_TOKEN;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.BEARER_TYPE;

/**
 * OAuth2 resource server configuration.
 */
@Configuration
@EnableResourceServer
public class OAuthResourceServerConfig extends ResourceServerConfigurerAdapter {

  private final String resourceId;
  private final ResourceServerTokenServices tokenServices;

  public OAuthResourceServerConfig(ServerProperties serverProperties, ResourceServerTokenServices tokenServices) {
    this.resourceId = serverProperties.getOAuth2().getResourceId();
    this.tokenServices = tokenServices;
  }

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) {
    resources.resourceId(resourceId)
      .tokenServices(tokenServices);
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.requestMatcher(new OAuthRequestMatcher())
      .authorizeRequests()
      .antMatchers(HttpMethod.OPTIONS).permitAll()
      .anyRequest().authenticated();
  }

  private static class OAuthRequestMatcher implements RequestMatcher {

    public boolean matches(HttpServletRequest request) {
      String auth = request.getHeader("Authorization");
      boolean hasTokenInHeader = (auth != null) && auth.toLowerCase(Locale.US).startsWith(BEARER_TYPE.toLowerCase(Locale.US));
      boolean hasTokenInParam = request.getParameter(ACCESS_TOKEN) != null;
      return hasTokenInHeader || hasTokenInParam;
    }
  }
}
