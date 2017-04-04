package com.faforever.server.rating;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.GamePlayerStats;
import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.Ladder1v1Rating;
import com.faforever.server.entity.Rating;
import com.faforever.server.error.ProgrammingError;
import jskills.GameInfo;
import jskills.IPlayer;
import jskills.ITeam;
import jskills.Player;
import jskills.Team;
import jskills.TrueSkillCalculator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Service for calculating game ratings, currently using TrueSkill.
 */
@Service
@Slf4j
public class RatingService {

  private final GameInfo gameInfo;

  public RatingService(ServerProperties properties) {
    ServerProperties.TrueSkill params = properties.getTrueSkill();
    gameInfo = new GameInfo(
      params.getInitialMean(),
      params.getInitialStandardDeviation(),
      params.getBeta(),
      params.getDynamicFactor(),
      params.getDrawProbability()
    );
  }

  public double calculateQuality(Rating left, Rating right) {
    jskills.Rating leftRating = ofNullable(left)
      .map(rating -> new jskills.Rating(left.getMean(), left.getDeviation()))
      .orElse(gameInfo.getDefaultRating());
    jskills.Rating rightRating = ofNullable(left)
      .map(rating -> new jskills.Rating(right.getMean(), right.getDeviation()))
      .orElse(gameInfo.getDefaultRating());

    Collection<ITeam> teams = Arrays.asList(
      new Team(new Player<>(1), leftRating),
      new Team(new Player<>(2), rightRating)
    );
    return TrueSkillCalculator.calculateMatchQuality(gameInfo, teams);
  }

  /**
   * Updates the ratings of all players in {@code playerStats} as well as their global/ladder1v1 rating,
   * according to the outcome of the game <strong> without persisting the results</strong>.
   *
   * @param noTeamId ID of the "no team" team
   */
  @SuppressWarnings("unchecked")
  public void updateRatings(Collection<GamePlayerStats> playerStats, int noTeamId, RatingType ratingType) {
    Map<Integer, GamePlayerStats> playerStatsByPlayerId = playerStats.stream()
      .collect(Collectors.toMap(stats -> stats.getPlayer().getId(), Function.identity()));

    Map<Integer, ScoredTeam> teamsById = teamsById(playerStats, noTeamId);

    List<ScoredTeam> teamsSortedByScore = new ArrayList<>(teamsById.values());
    teamsSortedByScore.sort(Comparator.comparingInt(o -> ((ScoredTeam) o).getScore().intValue()).reversed());

    int[] teamRanks = calculateTeamRanks(teamsSortedByScore);

    // New ArrayList is needed because the JSkill API doesn't handle subclass type parameters
    TrueSkillCalculator.calculateNewRatings(gameInfo, new ArrayList<>(teamsSortedByScore), teamRanks).entrySet()
      .forEach(entry -> {
        Player<Integer> player = (Player<Integer>) entry.getKey();
        GamePlayerStats stats = playerStatsByPlayerId.get(player.getId());

        updatePlayerStats(entry, stats);
        updateRating(ratingType, stats);
      });
  }

  public void initGlobalRating(com.faforever.server.entity.Player player) {
    Assert.state(player.getGlobalRating() == null, "Global rating has already been set for player: " + player);
    player.setGlobalRating(new GlobalRating(player, gameInfo.getInitialMean(), gameInfo.getInitialStandardDeviation()));
  }

  public void initLadder1v1Rating(com.faforever.server.entity.Player player) {
    Assert.state(player.getLadder1v1Rating() == null, "Ladder1v1 rating has already been set for player: " + player);
    player.setLadder1v1Rating(new Ladder1v1Rating(player, gameInfo.getInitialMean(), gameInfo.getInitialStandardDeviation()));
  }

  private Map<Integer, ScoredTeam> teamsById(Collection<GamePlayerStats> playerStats, int noTeamId) {
    int highestTeamId = playerStats.stream()
      .map(GamePlayerStats::getTeam)
      .max(Integer::compareTo).orElse(noTeamId);

    Map<Integer, ScoredTeam> teamsById = new HashMap<>();
    for (GamePlayerStats playerStat : playerStats) {
      IPlayer player = new Player<>(playerStat.getPlayer().getId());
      jskills.Rating rating = new jskills.Rating(playerStat.getMean(), playerStat.getDeviation());

      int teamId = playerStat.getTeam();
      if (teamId == noTeamId) {
        // If a player is in the "no team" team, assign him to his own team
        teamId = playerStat.getPlayer().getId() + highestTeamId;
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
      if (i == 0 || team.getScore().intValue() != teamsSortedByScore.get(i - 1).getScore().intValue()) {
        rank++;
      }
      teamRanks[i] = rank;
    }
    return teamRanks;
  }

  private void updatePlayerStats(Entry<IPlayer, jskills.Rating> entry, GamePlayerStats stats) {
    stats.setAfterMean(entry.getValue().getMean());
    stats.setAfterDeviation(entry.getValue().getStandardDeviation());
  }

  private void updateRating(RatingType ratingType, GamePlayerStats stats) {
    Rating rating;
    switch (ratingType) {
      case GLOBAL:
        rating = ofNullable(stats.getPlayer().getGlobalRating()).orElseGet(() -> {
          GlobalRating initial = new GlobalRating();
          stats.getPlayer().setGlobalRating(initial);
          return initial;
        });
        break;

      case LADDER_1V1:
        rating = ofNullable(stats.getPlayer().getLadder1v1Rating()).orElseGet(() -> {
          Ladder1v1Rating initial = new Ladder1v1Rating();
          stats.getPlayer().setLadder1v1Rating(initial);
          return initial;
        });
        break;

      default:
        throw new ProgrammingError("Uncovered rating type: " + ratingType);
    }
    rating.setMean(stats.getAfterMean());
    rating.setDeviation(stats.getAfterDeviation());

    log.debug("New '{}' rating for player '{}' is: {}", ratingType, stats.getPlayer(), rating);
  }

  @AllArgsConstructor
  @Getter
  private static class ScoredTeam extends Team {
    private final AtomicInteger score = new AtomicInteger();
    private final int id;
  }
}
