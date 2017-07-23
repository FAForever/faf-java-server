package com.faforever.server.integration.v2.server;


import com.faforever.server.entity.GameState;
import com.faforever.server.game.GameVisibility;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * Message sent from the server to the client containing information about an available featured mod.
 */
@Getter
@Setter
class GameInfoServerMessage extends V2ServerMessage {
  int id;
  String title;
  GameVisibility gameVisibility;
  Object password;
  GameState state;
  String featuredModTechnicalName;
  List<SimMod> simMods;
  String technicalMapName;
  String hostUsername;
  List<Player> players;
  int maxPlayers;
  Instant startTime;
  Integer minRating;
  Integer maxRating;
  List<FeaturedModFileVersion> featuredModFileVersions;

  @Getter
  @Setter
  static class Player {
    int team;
    String name;
  }

  @Getter
  @Setter
  static class SimMod {
    String uid;
    String displayName;
  }

  @Getter
  @Setter
  static class FeaturedModFileVersion {
    short id;
    int version;
  }
}
