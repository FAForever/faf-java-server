package com.faforever.server.game;

import com.faforever.server.entity.Game;
import com.faforever.server.response.ServerResponse;
import lombok.Data;

/**
 * Sends game information to the client.
 */
@Data
public class GameResponse implements ServerResponse {
  private final Game game;
}
