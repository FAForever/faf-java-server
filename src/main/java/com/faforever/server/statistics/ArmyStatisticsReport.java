package com.faforever.server.statistics;

import com.faforever.server.request.ClientMessage;
import lombok.Data;

import java.util.List;

@Data
public class ArmyStatisticsReport implements ClientMessage {
  private final List<ArmyStatistics> armyStatistics;
}
