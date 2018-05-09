package com.faforever.server.integration.v2.server;


import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client telling it to disconnect from a peer.
 */
@Getter
@Setter
@V2ServerResponse
class DisconnectPeerServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "disconnectFromPeer";

  /** ID of the player to disconnect. */
  int playerId;
}
