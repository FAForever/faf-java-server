package com.faforever.server.rating;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.GamePlayerStats;
import jskills.GameInfo;
import jskills.IPlayer;
import jskills.Player;
import jskills.Rating;
import jskills.Team;
import jskills.TrueSkillCalculator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for calculating game ratings, currently using TrueSkill.
 */
@Service
public class RatingService {

  private final GameInfo gameInfo;

  public RatingService(ServerProperties serverProperties) {
    ServerProperties.TrueSkill params = serverProperties.getTrueSkill();
    gameInfo = new GameInfo(
      params.getInitialMean(),
      params.getInitialStandardDeviation(),
      params.getBeta(),
      params.getDynamicFactor(),
      params.getDrawProbability()
    );
  }

  /**
   * Updates the ratings of all players in ${@code playerStats} according to the outcome of the game.
   *
   * @param noTeamId ID of the "no team" team
   */
  public void updateRatings(List<GamePlayerStats> playerStats, byte noTeamId) {
    Map<Integer, ScoredTeam> teamsById = teamsById(playerStats, noTeamId);

    List<ScoredTeam> teamsSortedByScore = new ArrayList<>(teamsById.values());
    teamsSortedByScore.sort(Comparator.comparingInt(o -> o.getScore().intValue()));

    int[] teamRanks = calculateTeamRanks(teamsSortedByScore);

    // New ArrayList is needed because the JSkill API doesn't handle subclass type parameters
    TrueSkillCalculator.calculateNewRatings(gameInfo, new ArrayList<>(teamsSortedByScore), teamRanks);
  }

  private Map<Integer, ScoredTeam> teamsById(List<GamePlayerStats> playerStats, byte noTeamId) {
    Map<Integer, ScoredTeam> teamsById = new HashMap<>();
    for (GamePlayerStats playerStat : playerStats) {
      IPlayer player = new Player<>(playerStat.getPlayer().getId());
      Rating rating = new Rating(playerStat.getMean(), playerStat.getDeviation());

      int teamId = playerStat.getTeam();
      if (teamId == noTeamId) {
        // If a player is in the "no team" team, assign him to his own team
        teamId = playerStat.getPlayer().getId();
      }
      teamsById.computeIfAbsent(teamId, ScoredTeam::new).addPlayer(player, rating);
      teamsById.get(teamId).getScore().updateAndGet(score -> score + playerStat.getScore());
    }
    return teamsById;
  }

  /**
   * Returns an array in the form of {@code [1, 2, 2, 3, 4]}.
   */
  private int[] calculateTeamRanks(List<ScoredTeam> teamsSortedByScore) {
    int[] teamRanks = new int[teamsSortedByScore.size()];
    int rank = 0;
    for (int i = 0; i < teamRanks.length; i++) {
      ScoredTeam team = teamsSortedByScore.get(i);
      if (i == 0 || team.getScore().intValue() < teamsSortedByScore.get(i - 1).getScore().intValue()) {
        rank++;
      }
      teamRanks[i] = rank;
    }
    return teamRanks;
  }

  @AllArgsConstructor
  @Getter
  private static class ScoredTeam extends Team {
    private final AtomicInteger score = new AtomicInteger();
    private final int id;
  }
}
