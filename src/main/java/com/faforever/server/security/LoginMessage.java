package com.faforever.server.security;

import com.faforever.server.request.ClientMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @deprecated the clien't shouldn't send login information, instead it should provide a JWT token acquired from the
 * API.
 */
@Getter
@RequiredArgsConstructor
@Deprecated
public class LoginMessage implements ClientMessage {

  private final String login;
  private final String password;
  private final String uniqueId;
}
