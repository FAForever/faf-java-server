package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Message sent from the client to the server informing it about a changed game option.
 */
@Getter
@AllArgsConstructor
class GameOptionClientMessage extends V2ClientMessage {
  /** The game option's key as in the game code. */
  private String key;
  /** The game option's value. */
  private String value;
}
