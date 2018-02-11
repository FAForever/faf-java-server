package com.faforever.server.coop;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * Reported by the game whenever a Co-Op operation completed.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoopMissionCompletedReport implements ClientMessage {
  /**
   * Whether primary targets where completed or not.
   */
  private boolean primaryTargets;
  /**
   * Whether secondary targets where completed or not.
   */
  private boolean secondaryTargets;
  /**
   * How long it took to finish the mission.
   */
  private Duration time;
}
