package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.game.StartGameProcessResponse;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public enum LaunchGameResponseTransformer implements GenericTransformer<StartGameProcessResponse, Map<String, Serializable>> {

  INSTANCE;

  @Override
  public Map<String, Serializable> transform(StartGameProcessResponse source) {
    return ImmutableMap.<String, Serializable>builder()
      .put("command", "game_launch")
      .put("mod", source.getMod())
      .put("uid", source.getGameId())
      .put("args", toLegacyArgs(source.getCommandLineArguments()))
      .build();
  }

  private String[] toLegacyArgs(List<String> commandLineArguments) {
    Assert.isTrue(commandLineArguments.size() == 2, "Legacy args used to have exactly two values");
    return new String[]{Joiner.on(' ').join(commandLineArguments)};
  }
}
