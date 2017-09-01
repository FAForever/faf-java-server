package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.player.LoginDetailsResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum LoginDetailsResponseTransformer implements GenericTransformer<LoginDetailsResponse, Map<String, Serializable>> {

  INSTANCE;

  @Override
  public Map<String, Serializable> transform(LoginDetailsResponse source) {
    return ImmutableMap.of(
      "command", "welcome",
      "id", source.getUserId(),
      "login", source.getUsername(),
      "me", PlayerInformationResponsesTransformer.player(source)
    );
  }
}
