package com.faforever.server.integration;

import com.faforever.server.response.ServerResponse;
import org.springframework.integration.annotation.Gateway;

public interface UserSession {

  @Gateway(requestChannel = "")
  void send(ServerResponse response);
}
