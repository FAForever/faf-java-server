package com.faforever.server.entity;

import com.faforever.server.game.Outcome;
import lombok.Data;

@Data
public class ArmyOutcome {
  private final int armyId;
  private final Outcome outcome;
}
