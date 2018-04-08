package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server informing it about a changed player option.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
class PlayerOptionClientMessage extends V2ClientMessage {
  /** ID of the player whose option has been changed. */
  private int playerId;
  /** The player option's key as in the game code. */
  private String key;
  /** The player option's value. */
  private String value;
}
