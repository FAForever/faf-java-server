package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import com.faforever.server.integration.v2.client.Faction;
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
   * Selected faction of the player.
   */
  private Faction faction;

  /**
   * Displayed name in the game (this is usually the FAF display name).
   */
  private String name;

  /**
   * The number of connected players required, before the game starts from automatch waiting state. Mandatory for
   * automatch games, otherwise {@code null}.
   */
  @Nullable
  private Integer expectedPlayers;

  /**
   * The players team number. Note: Offset by 1, since team 1 is free-for-all team.
   */
  private int team;

  /**
   * The players position on the map. If {@code null}, the game will use the map default positioning.
   */
  @Nullable
  private Integer mapPosition;

  /**
   * The command line arguments to append when invoking {@code ForgedAlliance.exe}.
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
     * The lobby is skipped; the game starts straight away.
     */
    NONE
  }
}
