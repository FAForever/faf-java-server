package com.faforever.server.rating;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.game.Game;
import com.faforever.server.game.GamePlayerStats;
import com.faforever.server.player.Player;
import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.faforever.server.game.GameService.NO_TEAM_ID;

public class RatingRecalculator {

  private Map<String, Player> players;
  private Map<String, Game> games;

  private void run() throws IOException {
    Stopwatch stopwatch = Stopwatch.createStarted();

    games = new HashMap<>();
    players = new HashMap<>();

    ServerProperties properties = new ServerProperties();
    RatingService ratingService = new RatingService(properties);

    System.out.println("Reading game player stats");
    Map<Game, List<GamePlayerStats>> statsByGame = readGamePlayerStats(ratingService);

    System.out.println(MessageFormat.format("Calculating {0,number} games", statsByGame.size()));
    statsByGame.values().parallelStream()
      .filter(stats -> stats.stream().collect(Collectors.groupingBy(GamePlayerStats::getTeam)).size() > 1)
      .filter(stats -> stats.stream().collect(Collectors.groupingBy(GamePlayerStats::getPlayer)).values().iterator().next().size() == 1)
      .peek(stats -> stats.forEach(stat -> {
        GlobalRating globalRating = stat.getPlayer().getGlobalRating();
        stat.setMean(globalRating.getMean());
        stat.setDeviation(globalRating.getDeviation());
      }))
      .forEach(stats -> ratingService.updateRatings(stats, NO_TEAM_ID, RatingType.GLOBAL));

    List<String> result = players.values().stream()
      .sorted(Comparator.comparingInt(Player::getId))
      .collect(Collectors.groupingByConcurrent(player -> (int) rating(player) / 100 * 100)).entrySet().stream()
      .map(ratingToPlayers -> new Object[]{ratingToPlayers.getKey(), ratingToPlayers.getValue().size()})
      .sorted(Comparator.comparingInt(value -> (int) value[0]))
      .map(ratingToCount -> String.format("%d,%d", (int) ratingToCount[0], (int) ratingToCount[1]))
      .collect(Collectors.toList());

    Files.write(Paths.get("build/histogram.csv"), result);

    System.out.println(stopwatch.stop());
  }

  private double rating(Player player) {
    return player.getGlobalRating().getMean() - 3 * player.getGlobalRating().getDeviation();
  }

  private Map<Game, List<GamePlayerStats>> readGamePlayerStats(RatingService ratingService) throws IOException {
    try (Stream<String> lines = Files.lines(Paths.get("C:\\tmp\\game_scores.csv"))) {
      return lines
        .map(s -> s.split(","))
        .map(strings -> new GamePlayerStats(
            games.computeIfAbsent(strings[1], id -> new Game(Integer.parseInt(id))),
            players.computeIfAbsent(strings[2], id -> {
              Player player = (Player) new Player().setId(Integer.parseInt(id));
              ratingService.initGlobalRating(player);
              return player;
            })
          )
            .setId(Integer.parseInt(strings[0]))
            .setGame(games.get(strings[1]))
            .setTeam(Integer.parseInt(strings[3]))
            .setScore(Integer.parseInt(strings[4]))
        )
        .collect(Collectors.groupingBy(GamePlayerStats::getGame));
    }
  }

  public static void main(String[] args) throws IOException {
    new RatingRecalculator().run();
  }
}
