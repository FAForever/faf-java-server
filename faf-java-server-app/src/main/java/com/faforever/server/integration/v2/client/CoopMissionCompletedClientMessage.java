package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server informing it about the results of a completed Co-Op mission.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientNotification
class CoopMissionCompletedClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "coopComplete";

  /** Whether primary targets where completed or not. */
  private boolean primaryTargets;

  /** Whether secondary targets where completed or not. */
  private boolean secondaryTargets;

  /** How long it took to finish the mission, in seconds. */
  private int time;
}
