package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.Data;

@Data
public class ArmyScoreReport implements ClientMessage {
  private final int armyId;
  private final int score;
}
