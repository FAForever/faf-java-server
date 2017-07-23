package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.ice.ForwardedIceMessage;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum ForwardedIceMessageTransformer implements GenericTransformer<ForwardedIceMessage, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(ForwardedIceMessage source) {
    return ImmutableMap.of(
      "command", "IceMsg",
      "target", "game",
      "args", new Object[]{source.getSenderId(), source.getContent()}
    );
  }
}
