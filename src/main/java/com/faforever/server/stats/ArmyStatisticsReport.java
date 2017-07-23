package com.faforever.server.stats;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArmyStatisticsReport implements ClientMessage {
  private List<ArmyStatistics> armyStatistics;
}
