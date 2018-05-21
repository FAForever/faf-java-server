package com.faforever.server.game;

import com.faforever.server.common.ServerMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Tells the Client to start a game process.
 */
@Getter
@AllArgsConstructor
public class StartGameProcessResponse implements ServerMessage {

  /** The technical name of the mod, e.g. "faf". */
  private final String mod;
  private final int gameId;

  /** Only set if the server decides which map will be played, e.g. in leaderboard games. */
  private final String mapFolderName;

  /**
   * @deprecated the server should never send command line arguments. They should always be generated on client side.
   * This is currently used for {@code /numgames} which shouldn't even be reported by a peer anyway, but looked up.
   */
  @Deprecated
  private final List<String> commandLineArguments;
}
