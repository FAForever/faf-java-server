package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.mod.FeaturedModResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum FeaturedModResponseTransformer implements GenericTransformer<FeaturedModResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(FeaturedModResponse source) {
    FeaturedMod mod = source.getFeaturedMod();
    return ImmutableMap.<String, Serializable>builder()
      .put("command", "mod_info")
      .put("publish", 1)
      .put("name", mod.getTechnicalName())
      .put("order", mod.getDisplayOrder())
      .put("fullname", mod.getDisplayName())
      .put("desc", mod.getDescription())
      .build();
  }
}
