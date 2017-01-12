package com.faforever.server.game;

import java.util.HashMap;
import java.util.Map;

public enum GameState {
  NEW(null),
  IDLE("Idle"),
  LOBBY("Lobby"),
  LAUNCHING("Launching"),
  ENDED("Ended");

  private static final Map<String, GameState> fromString;

  static {
    fromString = new HashMap<>();
    for (GameState state : values()) {
      fromString.put(state.string, state);
    }
  }

  /**
   * String as sent by the game.
   */
  private final String string;

  GameState(String string) {
    this.string = string;
  }

  public static GameState fromString(String string) {
    return fromString.get(string);
  }

  public String getString() {
    return string;
  }
}
