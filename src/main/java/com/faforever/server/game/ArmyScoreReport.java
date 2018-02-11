package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArmyScoreReport implements ClientMessage {
  private int armyId;
  private int score;
}
