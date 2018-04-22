package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArmyOutcomeReport implements ClientMessage {
  private int armyId;
  private Outcome outcome;
  private int score;
}
