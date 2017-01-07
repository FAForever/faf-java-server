package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.integration.legacy.dto.SessionResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum SessionResponseTransformer implements GenericTransformer<SessionResponse, Map<String, Serializable>> {

  INSTANCE;

  @Override
  public Map<String, Serializable> transform(SessionResponse source) {
    return ImmutableMap.<String, Serializable>builder()
      .put("command", "session")
      .put("session", source.getSessionId())
      .build();
  }
}
