package com.faforever.server.game;

import com.faforever.server.entity.GameState;
import com.faforever.server.response.ServerResponse;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Sends game information to the client.
 */
@Data
public class GameResponse implements ServerResponse {
  private final int id;
  private final String title;
  private final GameVisibility gameVisibility;
  private final Object password;
  private final GameState state;
  private final String featuredModTechnicalName;
  private final List<String> simMods;
  private final String technicalMapName;
  private final String hostUsername;
  private final List<Player> players;
  private final int maxPlayers;
  private final Instant startTime;
  private final Integer minRating;
  private final Integer maxRating;

  @Data
  public static class Player {
    private final int team;
    private final String name;
  }
}
