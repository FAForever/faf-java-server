package com.faforever.server.game;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The state of a running instance of Forged Alliance.
 */
public enum PlayerGameState {
  /** There is no game process, so there is no state. */
  NONE(null),

  /** The game process is starting but has not yet started. */
  INITIALIZING("Initializing", NONE),

  /**
   * The game process has been started and is waiting for instructions.
   *
   * @deprecated it's the client's responsibility to tell the game what to do
   */
  @Deprecated
  IDLE("Idle", NONE, INITIALIZING),

  /** The game process opened the game lobby. */
  LOBBY("Lobby", NONE, INITIALIZING, IDLE),

  /**
   * The game process has left the lobby and is now launching the game and its simulation. For unknown reasons, the game
   * doesn't have a state "Playing", so every game that is "Launching" is actually playing. Hopefully, this will follow
   * in future.
   */
  LAUNCHING("Launching", LOBBY),

  /**
   * The game simulation has ended but the game process is still running. Currently, the game does not send this command
   * but the client does when the process closed.
   */
  ENDED("Ended", LAUNCHING),

  /** The game process has been closed. */
  CLOSED("Closed", NONE, INITIALIZING, IDLE, LOBBY, LAUNCHING, ENDED);

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

  public String getString() {
    return string;
  }

  public static PlayerGameState fromString(String string) {
    return fromString.get(string);
  }

  /**
   * Checks whether a player's game is allowed to transition from an old state into a new state. If not, an {@link
   * IllegalStateException} is thrown.
   */
  public static void verifyTransition(@NotNull PlayerGameState oldState, @NotNull PlayerGameState newState) {
    Assert.state(canTransition(oldState, newState), "Can't transition from " + oldState + " to " + newState);
  }

  /**
   * Checks whether a player's game is allowed to transition from an old state into a new state.
   */
  public static boolean canTransition(@NotNull PlayerGameState oldState, @NotNull PlayerGameState newState) {
    return newState == NONE
      || newState.transitionsFrom.contains(oldState);
  }
}
