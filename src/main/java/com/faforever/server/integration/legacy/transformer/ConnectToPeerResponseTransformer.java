package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.ConnectToPeerResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum ConnectToPeerResponseTransformer implements GenericTransformer<ConnectToPeerResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(ConnectToPeerResponse source) {
    return ImmutableMap.of(
      "command", "ConnectToPeer",
      "args", new Object[]{source.getPlayerName(), source.getPlayerId(), source.isOffer()},
      "target", "game"
    );
  }
}
