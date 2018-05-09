package com.faforever.server.integration.v2.client;


import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Message sent from the client to the server informing it about an army outcome.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientNotification
class ArmyOutcomeClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "armyOutcomeReport";

  /** The ID of the army affected by this report. */
  private int armyId;
  /** Whether the army was victorious or not. */
  @NotNull
  private Outcome outcome;
  /** The army's final game score. */
  private int score;

  public enum Outcome {
    /** The army has been defeated. */
    DEFEAT,
    /** The army won the game. */
    VICTORY,
    /** The army hasn't won nor lost the game. */
    DRAW
  }
}
