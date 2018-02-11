package com.faforever.server.integration.v2.server;

import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client containing an info message to be displayed to the user.
 */
@Getter
@Setter
class HostGameServerMessage extends V2ServerMessage {
  /** (Folder) name of the map to be played. */
  String mapName;
}
