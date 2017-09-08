package com.faforever.server.game;

import com.faforever.server.common.ServerMessage;
import com.faforever.server.entity.GameState;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Sends game information to the client. Multiple objects should be wrapped in a {@link
 * com.faforever.server.client.GameResponses}.
 */
@Data
public class GameResponse implements ServerMessage {
  private final int id;
  private final String title;
  private final GameVisibility gameVisibility;
  private final Object password;
  private final GameState state;
  private final String featuredModTechnicalName;
  private final List<SimMod> simMods;
  private final String technicalMapName;
  private final String hostUsername;
  private final List<Player> players;
  private final int maxPlayers;
  private final Instant startTime;
  private final Integer minRating;
  private final Integer maxRating;
  private final List<FeaturedModFileVersion> featuredModFileVersions;

  @Data
  public static class Player {
    private final int team;
    private final String name;
  }

  @Data
  public static class SimMod {
    private final String uid;
    private final String displayName;
  }


  @Data
  public static class FeaturedModFileVersion {
    private final short id;
    private final int version;
  }
}
