package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Results of a finished game.
 */
@Getter
@Setter
@V2ServerResponse
class GameResultMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "gameResult";

  /** ID of the game. */
  private int gameId;

  /** {@code true} if there is no winner, {@code false} otherwise. */
  private boolean draw;

  /** The list of player results. */
  private Set<PlayerResult> playerResults;

  /** Represents a player result. */
  @Getter
  @Setter
  public static class PlayerResult {
    /** The ID of the player. */
    private int playerId;
    /** {@code true} if the player won, {@code false} otherwise. */
    private boolean winner;
    /** {@code true} if the player's ACU got killed, {@code false} otherwhise. */
    private boolean acuKilled;
  }
}
