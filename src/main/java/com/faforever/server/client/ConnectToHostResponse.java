package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

@Data
public class ConnectToHostResponse implements ServerMessage {
  private final int hostId;
}
