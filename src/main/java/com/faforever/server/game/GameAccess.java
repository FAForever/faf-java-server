package com.faforever.server.game;

import java.util.HashMap;
import java.util.Map;

public enum GameAccess {
  PUBLIC("public"),
  PRIVATE("private");

  private static final Map<String, GameAccess> fromString;

  static {
    fromString = new HashMap<>();
    for (GameAccess gameAccess : values()) {
      fromString.put(gameAccess.string, gameAccess);
    }
  }

  private final String string;

  GameAccess(String string) {
    this.string = string;
  }

  public static GameAccess fromString(String string) {
    return fromString.get(string);
  }
}
