package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.DisconnectPlayerResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum DisconnectPeerResponseTransformer implements GenericTransformer<DisconnectPlayerResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(DisconnectPlayerResponse source) {
    return ImmutableMap.of(
      "command", "DisconnectPeer",
      "peer_id", source.getPlayerId()
    );
  }
}
