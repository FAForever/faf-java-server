package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamKillReport implements ClientMessage {
  private Duration time;
  private int victimId;
  private String victimName;
  private int killerId;
  private String killerName;
}
