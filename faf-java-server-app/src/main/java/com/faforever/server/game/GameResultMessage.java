package com.faforever.server.game;

import com.faforever.server.common.ServerMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Results of a finished game.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameResultMessage implements ServerMessage {
  private int gameId;
  private boolean draw;
  private Set<PlayerResult> playerResults;

  @Getter
  @Setter
  public static class PlayerResult {
    private int playerId;
    private boolean winner;
    private boolean acuKilled;
  }
}
