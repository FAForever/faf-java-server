package com.faforever.server.entity;

import java.util.HashMap;
import java.util.Map;

public enum VictoryCondition {
  // Order is crucial
  DEMORALIZATION("demoralization"),
  DOMINATION("domination"),
  ERADICATION("eradication"),
  SANDBOX("sandbox");

  public static final String GAME_OPTION_NAME = "Victory";
  private static final Map<String, VictoryCondition> fromString;

  static {
    fromString = new HashMap<>();
    for (VictoryCondition condition : values()) {
      fromString.put(condition.string, condition);
    }
  }

  private final String string;

  VictoryCondition(String string) {
    this.string = string;
  }

  /**
   * Returns the {@link VictoryCondition} for a string sent by the game.
   */
  public static VictoryCondition fromString(String string) {
    return fromString.get(string);
  }
}
