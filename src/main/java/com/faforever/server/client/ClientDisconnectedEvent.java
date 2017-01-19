package com.faforever.server.client;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ClientDisconnectedEvent extends ApplicationEvent {
  private final ClientConnection clientConnection;

  public ClientDisconnectedEvent(Object source, ClientConnection clientConnection) {
    super(source);
    this.clientConnection = clientConnection;
  }
}
