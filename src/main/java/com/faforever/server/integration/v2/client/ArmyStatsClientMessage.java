package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server informing it about army statistics.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
class ArmyStatsClientMessage extends V2ClientMessage {
  private String stats;
}
