package com.faforever.server.game;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The state of a running instance of Forged Alliance.
 */
public enum PlayerGameState {
  UNKNOWN(null),
  IDLE("Idle", UNKNOWN),
  LOBBY("Lobby", UNKNOWN, IDLE),
  LAUNCHING("Launching", LOBBY),
  ENDED("Ended", UNKNOWN, IDLE, LAUNCHING, LOBBY);

  private static final Map<String, PlayerGameState> fromString;

  static {
    fromString = new HashMap<>();
    for (PlayerGameState state : values()) {
      fromString.put(state.string, state);
    }
  }

  /**
   * String as sent by the game.
   */
  private final String string;
  private final Collection<PlayerGameState> transitionsFrom;

  PlayerGameState(String string, PlayerGameState... transitionsFrom) {
    this.string = string;
    this.transitionsFrom = Arrays.asList(transitionsFrom);
  }

  public static PlayerGameState fromString(String string) {
    return fromString.get(string);
  }

  /**
   * Checks whether a player's game is allowed to transition from an old state into a new state. If not, an
   * {@link IllegalStateException} is thrown.
   */
  public static void verifyTransition(PlayerGameState oldState, PlayerGameState newState) {
    Assert.state(newState.transitionsFrom.contains(oldState), "Can't transition from " + oldState + " to " + newState);
  }

  public String getString() {
    return string;
  }
}
