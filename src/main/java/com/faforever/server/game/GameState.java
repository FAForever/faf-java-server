package com.faforever.server.game;

import java.util.HashMap;
import java.util.Map;

public enum GameState {
  IDLE("Idle"),
  LOBBY("Lobby"),
  PLAYING("Playing"),
  ENDED("Ended");

  private static final Map<String, GameState> fromString;

  static {
    fromString = new HashMap<>();
    for (GameState state : values()) {
      fromString.put(state.string, state);
    }
  }

  private final String string;

  GameState(String string) {
    this.string = string;
  }

  public static GameState fromString(String string) {
    return fromString.get(string);
  }
}
