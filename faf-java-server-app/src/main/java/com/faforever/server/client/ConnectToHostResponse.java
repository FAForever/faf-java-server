package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;
import lombok.Value;

@Value
public class ConnectToHostResponse implements ServerMessage {
  String hostUsername;
  int hostId;
}
