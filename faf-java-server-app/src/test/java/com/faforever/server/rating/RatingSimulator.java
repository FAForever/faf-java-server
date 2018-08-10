package com.faforever.server.rating;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.game.Game;
import com.faforever.server.game.GamePlayerStats;
import com.faforever.server.player.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.faforever.server.game.GameService.NO_TEAM_ID;

public class RatingSimulator {
  public static void main(String[] args) {
    RatingService ratingService = new RatingService(new ServerProperties());

    List<Player> players = IntStream.range(0, 10_000)
      .mapToObj(playerId -> {
        Player player = (Player) new Player().setId(playerId);
        ratingService.initGlobalRating(player);
        return player;
      }).collect(Collectors.toList());

    IntStream.range(0, 1_000_000).forEach(gameId -> {
      Game game = new Game(gameId);

      Player player1, player2;
      do {
        player1 = players.get((int) (Math.random() * players.size()));
        player2 = players.get((int) (Math.random() * players.size()));
      } while (player1 == player2);

      Collection<GamePlayerStats> playerStats = Arrays.asList(
        new GamePlayerStats(game, player1)
          .setMean(player1.getGlobalRating().getMean())
          .setDeviation(player1.getGlobalRating().getDeviation())
          .setScore(player1.getId() > player2.getId() ? 10 : -1)
          .setTeam(NO_TEAM_ID),
        new GamePlayerStats(game, player2)
          .setMean(player2.getGlobalRating().getMean())
          .setDeviation(player2.getGlobalRating().getDeviation())
          .setScore(player2.getId() > player1.getId() ? 10 : -1)
          .setTeam(NO_TEAM_ID)
      );

      ratingService.updateRatings(playerStats, NO_TEAM_ID, RatingType.GLOBAL);
    });

    players.stream()
      .filter(player -> player.getId() % 10 == 0)
      .forEach(player -> System.out.printf("Player %d: %s, rating: %f%n", player.getId(), player.getGlobalRating(), player.getGlobalRating().getMean() - 3 * player.getGlobalRating().getDeviation()));
  }
}
