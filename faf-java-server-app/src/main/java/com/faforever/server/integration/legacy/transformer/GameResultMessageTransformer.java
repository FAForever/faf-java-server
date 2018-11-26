package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.game.GameResultResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum GameResultMessageTransformer implements GenericTransformer<GameResultResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(GameResultResponse source) {
    return ImmutableMap.of(
      "command", "GameResult"
    );
  }
}
