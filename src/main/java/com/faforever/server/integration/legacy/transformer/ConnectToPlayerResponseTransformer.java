package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.ConnectToPlayerResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum ConnectToPlayerResponseTransformer implements GenericTransformer<ConnectToPlayerResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(ConnectToPlayerResponse source) {
    return ImmutableMap.of(
      "command", "ConnectToPeer",
      "args", new Object[]{source.getPlayerName(), source.getPlayerId()}
    );
  }
}
