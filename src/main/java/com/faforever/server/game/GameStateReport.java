package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.Data;

@Data
public class GameStateReport implements ClientMessage {

  private final PlayerGameState state;
}
