package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Message sent from the client to the server to inform the server that a player slot within the game lobby has been
 * cleared.
 */
@Getter
@AllArgsConstructor
class ClearSlotClientMessage extends V2ClientMessage {
  /** The ID of the game slot that has been cleared. */
  private int slotId;
}
