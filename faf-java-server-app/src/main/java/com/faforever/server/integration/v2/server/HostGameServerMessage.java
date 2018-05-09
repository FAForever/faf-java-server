package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client telling it to host a game.
 */
@Getter
@Setter
@V2ServerResponse
public class HostGameServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "hostGame";

  /** (Folder) name of the map to be played. */
  String mapName;
}
