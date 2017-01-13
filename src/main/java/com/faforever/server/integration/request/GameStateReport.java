package com.faforever.server.integration.request;

import com.faforever.server.game.PlayerGameState;
import com.faforever.server.request.ClientMessage;
import lombok.Data;

@Data
public class GameStateReport implements ClientMessage {

  private final PlayerGameState state;
}
