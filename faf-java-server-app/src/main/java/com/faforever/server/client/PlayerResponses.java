package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;
import com.faforever.server.player.PlayerResponse;
import lombok.Data;

import java.util.Collection;

@Data
public class PlayerResponses implements ServerMessage {
  private final Collection<PlayerResponse> responses;

  public PlayerResponses(Collection<PlayerResponse> responses) {
    this.responses = responses;
  }
}
