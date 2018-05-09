package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.game.HostGameResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum HostGameResponseTransformer implements GenericTransformer<HostGameResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(HostGameResponse source) {
    return ImmutableMap.of(
      "command", "HostGame",
      "target", "game",
      "args", new String[]{source.getMapFilename()}
    );
  }
}
