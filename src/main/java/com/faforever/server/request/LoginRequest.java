package com.faforever.server.request;

import lombok.Getter;

@Getter
public class LoginRequest extends ClientRequest {

  private final String login;
  private final String password;
  private final String uniqueId;

  public LoginRequest(String login, String password, String uniqueId) {
    this.login = login;
    this.password = password;
    this.uniqueId = uniqueId;
  }
}
