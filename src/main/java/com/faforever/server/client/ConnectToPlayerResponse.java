package com.faforever.server.client;

import com.faforever.server.response.ServerResponse;
import lombok.Data;

@Data
public class ConnectToPlayerResponse implements ServerResponse {
  private final String playerName;
  private final int playerId;
}
