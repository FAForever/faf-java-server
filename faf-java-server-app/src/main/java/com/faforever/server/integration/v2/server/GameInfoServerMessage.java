package com.faforever.server.integration.v2.server;


import com.faforever.server.annotations.V2ServerResponse;
import com.faforever.server.game.GameState;
import com.faforever.server.game.GameVisibility;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

/**
 * Message sent from the server to the client containing information about a game.
 */
@Getter
@Setter
@V2ServerResponse
class GameInfoServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "game";

  /** The game's ID, e.g. {@code 951989}. */
  int id;
  /** The game's title, e.g. {@code All Welcome}. */
  @NotNull
  String title;
  /** Specifies who can see the game, e.g. {@code PUBLIC}. */
  @NotNull
  GameVisibility gameVisibility;
  /** {@code true} if the game requires a password in order to join. */
  boolean passwordProtected;
  /** The current game state, e.g. {@code OPEN}. */
  @NotNull
  GameState state;
  /** The technical name of the game's featured mod, e.g. {@code faf}. */
  @NotNull
  String mod;
  /** The list of simulation mods enabled in this game. */
  @NotNull
  List<SimMod> simMods;
  /** The folder name of the map played in this game, e.g. {@code africa_ultimate.v0002} */
  @NotNull
  String map;
  /** The name of the player who created the game. */
  @NotNull
  String hostUsername;
  /** The players that are currently part of this game. */
  @NotNull
  List<Player> players;
  /** The maximum number of players who can play on the currently selected map. */
  int maxPlayers;
  /** When the game has been started, e.g. {@code 2018-05-09T22:53}. */
  @NotNull
  Instant startTime;
  /** The minimum rating participating players should have, as desired by the host of the game. E.g. {@code 800}. */
  Integer minRating;
  /** The maximum rating participating players should have, as desired by the host of the game. E.g. {@code 1200}. */
  Integer maxRating;

  /**
   * Legacy. The version of every file that make up the desired version of the game.
   *
   * @deprecated the client should instead read {@code modVersion} and ask the API for that version of the game and
   * download all files presented to it.
   */
  @Deprecated(forRemoval = true)
  @NotNull
  List<FeaturedModFileVersion> modFileVersions;

  /** The version of the game's featured mod, e.g. {@code 3765}. */
  int modVersion;

  /** A player within a game. */
  @Getter
  @Setter
  static class Player {
    /** The player's ID. */
    int id;
    /** The team the player is currently assigned to. 1 for "no team", -1 for "observer", >= 2 for teams >=1. */
    int team;
  }

  /** A simulation mod. */
  @Getter
  @Setter
  static class SimMod {
    /** The simulation mod's unique identifier. */
    String uid;
    /** The simulation mod's human-readable display name. */
    String displayName;
  }

  /** Specifies a version of a featured mod file. */
  @Getter
  @Setter
  static class FeaturedModFileVersion {
    /** The ID of the featured mod file. */
    short id;
    /** The version of the featured mod file. */
    int version;
  }
}
