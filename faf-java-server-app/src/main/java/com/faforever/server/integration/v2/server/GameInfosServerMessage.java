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
class GameInfosServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "games";

  /** The list of games. */
  List<GameInfoServerMessage> games;
}
