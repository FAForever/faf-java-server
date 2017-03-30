package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.chat.JoinChatChannelResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public enum JoinChatChannelsResponseTransformer implements GenericTransformer<JoinChatChannelResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(JoinChatChannelResponse source) {
    ArrayList<String> channels = new ArrayList<>(source.getChannels());
    return ImmutableMap.of(
      "command", "social",
      "channels", channels,
      "autojoin", channels
    );
  }
}
