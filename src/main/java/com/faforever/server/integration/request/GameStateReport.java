package com.faforever.server.integration.request;

import com.faforever.server.common.ClientMessage;
import com.faforever.server.game.PlayerGameState;
import lombok.Data;

@Data
public class GameStateReport implements ClientMessage {

  private final PlayerGameState state;
}
