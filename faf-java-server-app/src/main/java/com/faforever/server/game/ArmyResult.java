package com.faforever.server.game;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ArmyResult {

  int armyId;
  /** The army's outcome, updated whenever an army has been defeated or won the game. */
  @NotNull
  Outcome outcome;
  /** The army's score, updated whenever an army defeated someone or has been defeated. */
  @Nullable
  Integer score;

  public static ArmyResult of(int armyId, @NotNull Outcome outcome, @Nullable Integer score) {
    Assert.notNull(outcome, "'outcome' must not be null");

    return new ArmyResult(armyId, outcome, score);
  }
}
