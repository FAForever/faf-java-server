package com.faforever.server.integration.v2.server;

import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client to tell it to connect to the host of a game.
 */
@Getter
@Setter
class ConnectToHostServerMessage extends V2ServerMessage {
  /** The name of the host to connect to. */
  String hostUsername;
  /** The player ID of the host to connect to. */
  int hostId;
}
