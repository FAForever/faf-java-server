package com.faforever.server.game;

import com.faforever.server.request.ClientMessage;
import lombok.Data;

@Data
public class CloseGameRequest implements ClientMessage {
  private final int playerId;
}
