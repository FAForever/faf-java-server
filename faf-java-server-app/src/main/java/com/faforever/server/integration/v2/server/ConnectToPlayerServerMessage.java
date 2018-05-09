package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client telling it to connect to another player (in game).
 */
@Getter
@Setter
@V2ServerResponse
class ConnectToPlayerServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "connectToPeer";

  /** The name of the player to connect to. */
  String playerName;
  /** The ID of the player to connect to. */
  int playerId;
}
