package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.coop.CoopMissionResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum CoopMissionsResponseTransformer implements GenericTransformer<CoopMissionResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(CoopMissionResponse source) {
    return ImmutableMap.<String, Serializable>builder()
      .put("command", "coop_info")
      .put("featured_mod", "coop")
      .put("name", source.getName())
      .put("description", source.getDescription())
      .put("filename", source.getFilename())
      .build();
  }
}
