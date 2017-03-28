package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

@Data
public class ConnectToPlayerResponse implements ServerMessage {
  private final String playerName;
  private final int playerId;
}
