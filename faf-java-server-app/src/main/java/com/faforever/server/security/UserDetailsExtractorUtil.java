package com.faforever.server.security;

import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.security.Principal;
import java.util.Optional;

@UtilityClass
public class UserDetailsExtractorUtil {

  public Optional<FafUserDetails> extractUserDetailsOrNull(Principal principal) {
    if (principal instanceof OAuth2Authentication) {
      Object oAuthPrincipal = ((OAuth2Authentication) principal).getPrincipal();
      if (oAuthPrincipal instanceof FafUserDetails) {
        return Optional.of((FafUserDetails) oAuthPrincipal);
      }
    } else if (principal instanceof UsernamePasswordAuthenticationToken) {
      Object tokenPrincipal = ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
      if (tokenPrincipal instanceof FafUserDetails) {
        return Optional.of((FafUserDetails) tokenPrincipal);
      }
    }
    return Optional.empty();
  }
}
