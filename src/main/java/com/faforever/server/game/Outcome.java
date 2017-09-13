package com.faforever.server.game;

import java.util.HashMap;
import java.util.Map;

public enum Outcome {
  UNKNOWN(null),
  DEFEAT("defeat"),
  VICTORY("victory"),
  DRAW("draw");

  private static final Map<String, Outcome> fromString;

  static {
    fromString = new HashMap<>();
    for (Outcome outcome : values()) {
      fromString.put(outcome.string, outcome);
    }
  }

  private final String string;

  Outcome(String string) {
    this.string = string;
  }

  public static Outcome fromString(String string) {
    return fromString.get(string);
  }
}
