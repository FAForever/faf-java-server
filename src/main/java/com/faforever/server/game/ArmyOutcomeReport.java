package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.Data;

@Data
public class ArmyOutcomeReport implements ClientMessage {
  private final int armyId;
  private final Outcome outcome;
}
