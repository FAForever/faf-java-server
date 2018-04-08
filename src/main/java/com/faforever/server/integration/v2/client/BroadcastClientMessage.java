package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to broadcast a text message to all connected clients.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
class BroadcastClientMessage extends V2ClientMessage {
  /** The message to be broadcasted. */
  private String message;
}
