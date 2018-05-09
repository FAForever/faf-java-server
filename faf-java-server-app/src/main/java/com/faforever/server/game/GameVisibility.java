package com.faforever.server.game;

import java.util.HashMap;
import java.util.Map;

public enum GameVisibility {
  /** The game is visible to everyone. */
  PUBLIC("public"),
  /** The game is only visible to friends. */
  FRIENDS("friends"),
  /** The game is not visible to anyone. */
  PRIVATE("private");

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

  public String getString() {
    return string;
  }

  public static GameVisibility fromString(String string) {
    return fromString.get(string);
  }
}
