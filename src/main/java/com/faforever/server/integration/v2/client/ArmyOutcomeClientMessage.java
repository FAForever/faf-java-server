package com.faforever.server.integration.v2.client;


import com.faforever.server.game.Outcome;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server informing it about an army outcome.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
class ArmyOutcomeClientMessage extends V2ClientMessage {
  /** The ID of the army affected by this report. */
  private int armyId;
  /** Whether the army was victorious or not. */
  private Outcome outcome;
  /** The army's final game score. */
  private int score;
}
