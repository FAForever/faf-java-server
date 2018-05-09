package com.faforever.server.security;

import com.faforever.server.entity.OAuthClient;
import com.faforever.server.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

/**
 * Converts a {@link FafUserDetails} from and to an {@link Authentication} for use in a JWT token.
 */
@Component
public class FafUserAuthenticationConverter extends DefaultUserAuthenticationConverter {

  @Override
  public Map<String, ?> convertUserAuthentication(Authentication authentication) {
    throw new UnsupportedOperationException("The server is not meant to generate JWT, only to parse them.");
  }

  @Override
  @SuppressWarnings("unchecked")
  public Authentication extractAuthentication(Map<String, ?> map) {
    Collection<? extends GrantedAuthority> authorities = getAuthorities(map);

    Object principal;
    if (map.containsKey(OAuth2Utils.CLIENT_ID)) {
      principal = extractFafClientDetails((Map<String, Object>) map);
    } else {
      principal = new FafUserDetails(new User());
    }

    return new PreAuthenticatedAuthenticationToken(principal, "N/A", authorities);
  }

  private FafClientDetails extractFafClientDetails(Map<String, Object> map) {
    OAuthClient client = new OAuthClient(
      (String) map.get(OAuth2Utils.CLIENT_ID),
      null,
      null,
      null,
      null,
      null
    );

    return new FafClientDetails(
      client,
      ((String) map.getOrDefault(OAuth2Utils.SCOPE, "")).replace(' ', ','),
      ((String) map.getOrDefault(OAuth2Utils.REDIRECT_URI, "")).replace(' ', ',')
    );
  }

  private Collection<? extends GrantedAuthority> getAuthorities(Map<String, ?> map) {
    if (!map.containsKey(AUTHORITIES)) {
      return Collections.emptySet();
    }
    Object authorities = map.get(AUTHORITIES);
    if (authorities instanceof String) {
      return commaSeparatedStringToAuthorityList((String) authorities);
    }
    if (authorities instanceof Collection) {
      return commaSeparatedStringToAuthorityList(StringUtils.collectionToCommaDelimitedString((Collection<?>) authorities));
    }
    throw new IllegalArgumentException("Authorities must be either a String or a Collection");
  }
}
