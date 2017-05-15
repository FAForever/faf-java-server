package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;
import com.faforever.server.player.PlayerInformationResponse;
import lombok.Data;

import java.util.Collection;

@Data
public class PlayerInformationResponses implements ServerMessage {
  private final Collection<PlayerInformationResponse> responses;

  public PlayerInformationResponses(Collection<PlayerInformationResponse> responses) {
    this.responses = responses;
  }
}
