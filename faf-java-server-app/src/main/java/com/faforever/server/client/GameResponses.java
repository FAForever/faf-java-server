package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;
import com.faforever.server.game.GameResponse;
import lombok.Data;

import java.util.Collection;

@Data
public class GameResponses implements ServerMessage {
  private final Collection<GameResponse> responses;

  public GameResponses(Collection<GameResponse> responses) {
    this.responses = responses;
  }
}
