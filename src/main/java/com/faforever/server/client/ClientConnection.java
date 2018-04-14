package com.faforever.server.client;

import com.faforever.server.integration.Protocol;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;

import java.net.InetAddress;
import java.time.Instant;

@Data
public class ClientConnection implements ConnectionAware {

  private final String id;
  private final Protocol protocol;
  private final InetAddress clientAddress;
  private String userAgent;
  /** When the last message was received from the client. */
  private Instant lastSeen;
  @Nullable
  private Authentication authentication;

  @Override
  public ClientConnection getClientConnection() {
    return this;
  }
}
