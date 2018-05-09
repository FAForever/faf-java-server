package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Message sent from the server to the client containing information about players.
 */
@Getter
@Setter
@V2ServerResponse
class PlayersServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "players";

  /** The list of players. */
  List<PlayerServerMessage> players;
}
