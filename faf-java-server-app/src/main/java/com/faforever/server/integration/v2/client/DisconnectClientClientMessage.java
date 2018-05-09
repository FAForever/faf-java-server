package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to request disconnecting a client from the server.
 */
@Getter
@AllArgsConstructor
@V2ClientNotification
@NoArgsConstructor
class DisconnectClientClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "disconnectClient";

  /** ID of the player whose client should be disconnected. */
  private int playerId;
}
