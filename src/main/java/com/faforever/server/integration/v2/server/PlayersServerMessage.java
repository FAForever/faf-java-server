package com.faforever.server.integration.v2.server;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Message sent from the server to the client containing information about players.
 */
@Getter
@Setter
class PlayersServerMessage extends V2ServerMessage {

  List<PlayerServerMessage> players;
}
