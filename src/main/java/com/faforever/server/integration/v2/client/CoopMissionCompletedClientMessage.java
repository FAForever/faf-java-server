package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Message sent from the client to the server informing it about the results of a completed Co-Op mission.
 */
@Getter
@AllArgsConstructor
class CoopMissionCompletedClientMessage extends V2ClientMessage {
  /**
   * Whether primary targets where completed or not.
   */
  private boolean primaryTargets;
  /**
   * Whether secondary targets where completed or not.
   */
  private boolean secondaryTargets;
  /**
   * How long it took to finish the mission, in seconds.
   */
  private int time;
}
