package com.faforever.server.integration.session;

import com.faforever.server.integration.Protocol;
import com.faforever.server.security.FafUserDetails;
import lombok.Data;

@Data
public class ClientConnection {

  /**
   * Name for the client connection header attribute.
   */
  public static final String CLIENT_CONNECTION = "clientConnection";

  private final String id;
  private final Protocol protocol;
  private FafUserDetails userDetails;
}
