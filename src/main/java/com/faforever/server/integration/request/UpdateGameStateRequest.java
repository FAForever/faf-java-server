package com.faforever.server.integration.request;

import com.faforever.server.game.GameState;
import lombok.Data;

@Data
public class UpdateGameStateRequest {

  private final int gameId;
  private final GameState gameState;
}
