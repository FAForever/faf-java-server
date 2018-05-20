package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to request disconnecting a client from the server.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
class DisconnectClientClientMessage extends V2ClientMessage {
  private int userId;
}
