package com.faforever.server.client;

import com.faforever.server.integration.Protocol;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;

import java.net.InetAddress;

@Data
public class ClientConnection implements ConnectionAware {

  private final String id;
  private final Protocol protocol;
  private final InetAddress clientAddress;
  private String userAgent;
  @Nullable
  private Authentication authentication;

  @Override
  public ClientConnection getClientConnection() {
    return this;
  }
}
