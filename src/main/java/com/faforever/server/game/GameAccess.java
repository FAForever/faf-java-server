package com.faforever.server.game;

import java.util.HashMap;
import java.util.Map;

// TODO rethink what game access levels make sense
public enum GameAccess {
  /** Public games can be joined by everyone. */
  PUBLIC("public"),
  /** Private games can only be joined by the host's friends. */
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
