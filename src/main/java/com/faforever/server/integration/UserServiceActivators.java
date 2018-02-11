package com.faforever.server.integration;

import com.faforever.server.client.LoginRequest;
import com.faforever.server.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

@Slf4j
@MessageEndpoint
public class UserServiceActivators {

  private final UserService userService;

  public UserServiceActivators(UserService userService) {
    this.userService = userService;
  }

  @ServiceActivator(inputChannel = ChannelNames.LOGIN_REQUEST)
  public void loginRequest(LoginRequest request) {
    userService.login(request.getUniqueId(), request.getJwt());
  }
}
