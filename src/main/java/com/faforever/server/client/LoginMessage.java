package com.faforever.server.client;

import com.faforever.server.common.ClientMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @deprecated the client shouldn't send login information, instead it should provide a JWT token acquired from the
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
