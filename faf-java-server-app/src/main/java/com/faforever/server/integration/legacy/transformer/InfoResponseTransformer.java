package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.InfoResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum InfoResponseTransformer implements GenericTransformer<InfoResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(InfoResponse source) {
    return ImmutableMap.of(
      "command", "notice",
      "style", "info",
      "text", source.getMessage()
    );
  }
}
