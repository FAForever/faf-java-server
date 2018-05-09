package com.faforever.server.client;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Fired when a connection to a client needs to be closed.
 */
@Getter
public class CloseConnectionEvent extends ApplicationEvent {
  /**
   * The connection to close.
   */
  private final ClientConnection clientConnection;

  public CloseConnectionEvent(Object source, ClientConnection clientConnection) {
    super(source);
    this.clientConnection = clientConnection;
  }
}
