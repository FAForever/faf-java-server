package com.faforever.server.game;

import com.faforever.server.request.ClientMessage;
import lombok.Data;

import java.time.Duration;

@Data
public class TeamKillReport implements ClientMessage {
  private final Duration time;
  private final int victimId;
  private final String victimName;
  private final int killerId;
  private final String killerName;
}
