package com.faforever.server.integration.v2.client;


import com.faforever.server.game.Outcome;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Message sent from the client to the server informing it about army statistics.
 */
@Getter
@AllArgsConstructor
class ArmyOutcomeClientMessage extends V2ClientMessage {
  private int armyId;
  private Outcome outcome;
}
