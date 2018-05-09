package com.faforever.server.client;

import com.faforever.server.player.PlayerResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Aggregates a list of {@link PlayerResponse} to a single {@link PlayerResponses}.
 */
public enum PlayerResponseAggregator implements DelayedResponseAggregator<PlayerResponse, PlayerResponses> {
  INSTANCE;

  @Override
  public PlayerResponses apply(List<DelayedResponse<PlayerResponse>> delayedResponses) {
    return new PlayerResponses(delayedResponses.stream()
      .map(DelayedResponse::getResponse)
      .collect(Collectors.toList()));
  }
}
