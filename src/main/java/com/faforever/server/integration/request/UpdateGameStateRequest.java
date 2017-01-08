package com.faforever.server.integration.request;

import com.faforever.server.game.GameState;
import com.faforever.server.request.ClientRequest;
import lombok.Data;

@Data
public class UpdateGameStateRequest implements ClientRequest {

  private final GameState gameState;
}
