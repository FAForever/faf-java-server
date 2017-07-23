package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Message sent from the client to the server to request disconnecting a client from the server.
 */
@Getter
@AllArgsConstructor
class DisconnectClientClientMessage extends V2ClientMessage {
  private int userId;
}
