package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.ConnectToHostResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum ConnectToHostResponseTransformer implements GenericTransformer<ConnectToHostResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(ConnectToHostResponse source) {
    return ImmutableMap.of(
      "command", "JoinGame",
      "args", new Object[]{source.getHostUsername(), source.getHostId()},
      "target", "game"
    );
  }
}
