package com.faforever.server.security;

import com.faforever.server.request.ClientRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginRequest implements ClientRequest {

  private final String login;
  private final String password;
  private final String uniqueId;
}
