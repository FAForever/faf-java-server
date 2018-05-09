package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Message sent from the server to the client to tell it to launch its game process.
 */
@Getter
@Setter
@V2ServerResponse
class StartGameProcessServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "startGameProcess";

  /** The technical name of the mod, e.g. "faf". */
  private String mod;
  /** The ID of the game that will be played. */
  private int gameId;
  /**
   * The folder name of the map (e.g. {@code SCMP_001}) to play. Only set in case of server-initiated matches, like
   * leaderboard games.
   */
  @Nullable
  private String map;

  /** In which lobby mode to start the game in. */
  @Nullable
  private LobbyMode lobbyMode;

  /**
   * private LobbyMode lobbyMode;
   * <p>
   * /**
   *
   * @deprecated the server should never send command line arguments. They should always be generated on client side.
   * This is currently used for {@code /numgames} which shouldn't even be reported by a peer anyway, but looked up.
   */
  @Deprecated
  private List<String> commandLineArguments;

  /**
   * See values for description.
   */
  public enum LobbyMode {

    /**
     * Default lobby where players can select their faction, teams and so on.
     */
    DEFAULT,

    /**
     * The lobby is skipped; the game starts straight away,
     */
    NONE
  }
}
