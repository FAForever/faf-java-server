package com.faforever.server.entity;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;

/**
 * The state of a game as stored on the server.
 */
public enum GameState {
  /** The game has been created but the host's game process has not yet started. */
  INITIALIZING(),
  /** The game has been created and the host's game process has been started. */
  OPEN(INITIALIZING),
  /** The game has been launched. */
  PLAYING(OPEN),
  /** The game simulation ended but not all players closed their game process yet. */
  ENDED(PLAYING),
  /** All players closed their game process. */
  CLOSED(INITIALIZING, OPEN, PLAYING, ENDED);

  private final Collection<GameState> transitionsFrom;

  GameState(GameState... transitionsFrom) {
    this.transitionsFrom = Arrays.asList(transitionsFrom);
  }

  /**
   * Checks whether a game is allowed to transition from an old state into a new state. If not, an {@link
   * IllegalStateException} is thrown.
   */
  public static void verifyTransition(GameState oldState, GameState newState) {
    Assert.state(newState.transitionsFrom.contains(oldState), "Can't transition from " + oldState + " to " + newState);
  }
}
