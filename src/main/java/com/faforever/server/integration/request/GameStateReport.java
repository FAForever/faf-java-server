package com.faforever.server.integration.request;

import com.faforever.server.game.GameState;
import com.faforever.server.request.ClientMessage;
import lombok.Data;

@Data
public class GameStateReport implements ClientMessage {

  private final GameState gameState;
}
