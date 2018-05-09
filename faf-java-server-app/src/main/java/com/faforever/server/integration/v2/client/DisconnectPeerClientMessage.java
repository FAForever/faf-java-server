package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to request players to close the connection to another player.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientNotification
class DisconnectPeerClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "disconnectPeer";

  /** The ID of the player who should be disconnected. */
  private int playerId;
}
