package com.faforever.server.coop;

import com.faforever.server.request.ClientMessage;
import lombok.Data;

import java.time.Duration;

/**
 * Reported by the game whenever a Co-Op operation completed.
 */
@Data
public class CoopMissionCompletedReport implements ClientMessage {
  /**
   * Whether primary targets where completed or not.
   */
  private final boolean primaryTargets;
  /**
   * Whether secondary targets where completed or not.
   */
  private final boolean secondaryTargets;
  /**
   * How long it took to finish the mission.
   */
  private final Duration duration;
}
