package com.faforever.server.client;

import com.faforever.server.integration.Protocol;
import com.faforever.server.security.FafUserDetails;
import lombok.Data;

@Data
public class ClientConnection {

  private final String id;
  private final Protocol protocol;
  private FafUserDetails userDetails;
}
