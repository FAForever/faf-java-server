package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.DisconnectPlayerFromGameResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum DisconnectPeerResponseTransformer implements GenericTransformer<DisconnectPlayerFromGameResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(DisconnectPlayerFromGameResponse source) {
    return ImmutableMap.of(
      "command", "DisconnectFromPeer",
      "args", new Object[]{source.getPlayerId()},
      "target", "game"
    );
  }
}
