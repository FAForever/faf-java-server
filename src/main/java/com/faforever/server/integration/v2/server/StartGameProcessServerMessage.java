package com.faforever.server.integration.v2.server;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Message sent from the server to the client to tell it to launch its game process.
 */
@Getter
@Setter
class StartGameProcessServerMessage extends V2ServerMessage {

  /** The technical name of the mod, e.g. "faf". */
  private String mod;
  /** The ID of the game that will be played. */
  private int gameId;
  /**
   * @deprecated the server should never send command line arguments. They should always be generated on client side.
   * This is currently used for {@code /numgames} which shouldn't even be reported by a peer anyway, but looked up.
   */
  @Deprecated
  private List<String> commandLineArguments;
}
