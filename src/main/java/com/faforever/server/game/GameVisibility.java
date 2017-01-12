package com.faforever.server.game;

import java.util.HashMap;
import java.util.Map;

public enum GameVisibility {
  PUBLIC("public"),
  FRIENDS("friends");

  private static final Map<String, GameVisibility> fromString;

  static {
    fromString = new HashMap<>();
    for (GameVisibility visibility : values()) {
      fromString.put(visibility.string, visibility);
    }
  }

  private final String string;

  GameVisibility(String string) {
    this.string = string;
  }

  public static GameVisibility fromString(String string) {
    return fromString.get(string);
  }

  public String getString() {
    return string;
  }
}
