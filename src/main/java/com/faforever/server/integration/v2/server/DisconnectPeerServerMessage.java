package com.faforever.server.integration.v2.server;


import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client telling it to disconnect from a peer.
 */
@Getter
@Setter
class DisconnectPeerServerMessage extends V2ServerMessage {
  /** ID of the player to disconnect. */
  int playerId;
}
