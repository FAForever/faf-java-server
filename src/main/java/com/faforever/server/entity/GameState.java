package com.faforever.server.entity;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;

/**
 * The state of a game as stored on the server.
 */
public enum GameState {
  INITIALIZING(),
  OPEN(INITIALIZING),
  PLAYING(OPEN),
  CLOSED(INITIALIZING, OPEN, PLAYING);

  private final Collection<GameState> transitionsFrom;

  GameState(GameState... transitionsFrom) {
    this.transitionsFrom = Arrays.asList(transitionsFrom);
  }

  /**
   * Checks whether a game is allowed to transition from an old state into a new state. If not, an
   * {@link IllegalStateException} is thrown.
   */
  public static void verifyTransition(GameState oldState, GameState newState) {
    Assert.state(newState.transitionsFrom.contains(oldState), "Can't transition from " + oldState + " to " + newState);
  }
}
