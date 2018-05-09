package com.faforever.server.client;

import com.faforever.server.game.GameResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Aggregates a list of {@link GameResponse} to a single {@link GameResponses}.
 */
public enum GameResponseAggregator implements DelayedResponseAggregator<GameResponse, GameResponses> {
  INSTANCE;

  @Override
  public GameResponses apply(List<DelayedResponse<GameResponse>> delayedResponses) {
    return new GameResponses(delayedResponses.stream()
      .map(DelayedResponse::getResponse)
      .collect(Collectors.toList()));
  }
}
