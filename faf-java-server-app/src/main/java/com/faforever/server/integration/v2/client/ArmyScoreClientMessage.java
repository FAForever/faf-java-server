package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server informing it about the updated score of an army.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientNotification
class ArmyScoreClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "armyScore";

  /** The ID of the army affected by this report. */
  private int armyId;

  /** The army's new score. */
  private int score;
}
