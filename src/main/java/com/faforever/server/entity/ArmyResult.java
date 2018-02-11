package com.faforever.server.entity;

import com.faforever.server.game.Outcome;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ArmyResult {

  private final int armyId;
  /** The army's outcome, updated whenever an army has been defeated or won the game. */
  @NotNull
  private final Outcome outcome;
  /** The army's score, updated whenever an army defeated someone or has been defeated. */
  @Nullable
  private final Integer score;

  public static ArmyResult of(int armyId, @NotNull Outcome outcome, @Nullable Integer score) {
    Assert.notNull(outcome, "'outcome' must not be null");

    return new ArmyResult(armyId, outcome, score);
  }
}
