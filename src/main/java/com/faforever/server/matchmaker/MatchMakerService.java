package com.faforever.server.matchmaker;

import com.faforever.server.client.ClientService;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Ladder1v1Rating;
import com.faforever.server.entity.Player;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.Requests;
import com.faforever.server.game.Faction;
import com.faforever.server.game.GameService;
import com.faforever.server.game.GameVisibility;
import com.faforever.server.map.MapService;
import com.faforever.server.mod.ModService;
import com.faforever.server.player.PlayerService;
import com.faforever.server.rating.RatingService;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Holds multiple match maker queues to which searches can be submitted. Once at least two searches are available in the
 * same queue, the service will calculate the rating between the two players. If a configured minimum quality is met,
 * the two players will get matched. The minimum quality required for a match will be reduced the longer a search
 * remains in the queue until a configured lowest value is reached.
 */
@Slf4j
@Service
public class MatchMakerService {

  private static final double DESIRED_MIN_GAME_QUALITY = 0.8d;
  private final ModService modService;
  private final ServerProperties properties;
  private final RatingService ratingService;
  private final Map<String, Queue<MatchMakerSearch>> searchesByQueueName;
  private final GameService gameService;
  private final MapService mapService;
  private final PlayerService playerService;
  private final ClientService clientService;
  private final Map<String, FeaturedMod> modByQueueName;

  public MatchMakerService(ModService modService, ServerProperties properties, RatingService ratingService, ClientService clientService, GameService gameService, MapService mapService, PlayerService playerService) {
    this.modService = modService;
    this.properties = properties;
    this.ratingService = ratingService;
    this.clientService = clientService;
    this.gameService = gameService;
    this.mapService = mapService;
    this.playerService = playerService;
    modByQueueName = new HashMap<>();
    searchesByQueueName = new HashMap<>();
  }

  @PostConstruct
  public void postConstruct() {
    modByQueueName.put("ladder1v1", modService.getLadder1v1());
    // Before adding additional queues here, be advised that this class currently uses ladder1v1Rating
  }

  /**
   * Submits a new search to the matchmaker, thus marking a player available for matchmaking.
   *
   * @param faction the faction the player will play once the game starts
   */
  public void submitSearch(Player player, Faction faction, String queueName) {
    Requests.verify(player.getCurrentGame() == null, ErrorCode.ALREADY_IN_GAME);
    Requests.verify(isNotBanned(player), ErrorCode.BANNED_FROM_MATCH_MAKER);
    Requests.verify(modByQueueName.get(queueName) != null, ErrorCode.MATCHMAKER_1V1_ONLY);

    MatchMakerSearch search = new MatchMakerSearch(Instant.now(), player, faction, queueName);
    synchronized (searchesByQueueName) {
      log.debug("Adding player '{}' to queue '{}'", search.player, search.queueName);
      searchesByQueueName.computeIfAbsent(queueName, s -> new LinkedList<>()).add(search);
      notifyPlayers(search, DESIRED_MIN_GAME_QUALITY);
    }
  }

  @Scheduled(fixedDelay = 3000)
  public void processQueue() {
    synchronized (searchesByQueueName) {
      log.trace("Processing '{}' queues", searchesByQueueName.size());
      searchesByQueueName.entrySet().forEach(entry -> {
        String queueName = entry.getKey();
        Queue<MatchMakerSearch> queue = entry.getValue();

        log.trace("Processing queue '{}'", queueName);
        queue.stream()
          .limit((queue.size() + 1) / 2)
          .map(this::findMatch)
          .filter(Optional::isPresent)
          .collect(Collectors.toList())
          .forEach(optional -> {
            Match match = optional.get();
            MatchMakerSearch leftSearch = match.leftSearch;
            MatchMakerSearch rightSearch = match.rightSearch;

            Player leftPlayer = leftSearch.player;
            Player rightPlayer = rightSearch.player;

            log.debug("Player '{}' was matched against '{}' with game quality '{}'", leftPlayer, rightPlayer, match.quality);
            queue.remove(leftSearch);
            queue.remove(rightSearch);

            int modId = modByQueueName.get(leftSearch.queueName).getId();
            startGame(modId, leftPlayer, leftSearch.faction, rightPlayer, rightSearch.faction);
          });
      });
    }
  }

  /**
   * Tries to find the best match for a {@code search}. The minimum quality required for a match depends on the outcome
   * of {@link #passesMinimumQuality(Match)}.
   */
  private Optional<Match> findMatch(MatchMakerSearch leftSearch) {
    log.trace("Trying to find match for search '{}'", leftSearch);

    Ladder1v1Rating defaultRating = (Ladder1v1Rating) new Ladder1v1Rating()
      .setMean(properties.getTrueSkill().getInitialStandardDeviation())
      .setDeviation(properties.getTrueSkill().getInitialStandardDeviation());

    String queueName = leftSearch.queueName;
    synchronized (searchesByQueueName) {
      return searchesByQueueName.get(queueName).stream()
        .filter(otherSearch -> !otherSearch.equals(leftSearch))
        .map(rightSearch -> new Match(
          leftSearch,
          rightSearch,
          ratingService.calculateQuality(
            Optional.ofNullable(leftSearch.player.getLadder1v1Rating()).orElse(defaultRating),
            Optional.ofNullable(rightSearch.player.getLadder1v1Rating()).orElse(defaultRating)
          )))
        .filter(this::passesMinimumQuality)
        .max(Comparator.comparingDouble(value -> value.quality));
    }
  }

  /**
   * Notify all players, except those already searching and the one who owns the search, about the {@code search} if
   * they would result in a game with a quality of at least {@code minQuality}.
   */
  private void notifyPlayers(MatchMakerSearch search, double minQuality) {
    playerService.getPlayers().parallelStream()
      .map(rightPlayer -> new PotentialMatch(
        rightPlayer,
        search.queueName,
        ratingService.calculateQuality(search.player.getLadder1v1Rating(), rightPlayer.getLadder1v1Rating())
      ))
      .filter(match -> match.quality > minQuality)
      .forEach(match -> clientService.sendMatchmakerNotification(match.queueName, match.rightPlayer));
  }

  /**
   * Tells one player to host a game and the other player to join the game.
   */
  private void startGame(int modId, Player host, Faction hostFaction, Player opponent, Faction opponentFaction) {
    gameService.createGame(
      host.getLogin() + " vs. " + opponent.getLogin(),
      modId,
      // TODO filename is probably the wrong parameter
      mapService.getRandomLadderMap().getFilename(),
      null,
      GameVisibility.PRIVATE,
      host
    ).thenAccept(game -> {
      Map<Integer, Map<String, Object>> playerOptions = game.getPlayerOptions();
      int playerId = host.getId();
      playerOptions.put(playerId, ImmutableMap.of(GameService.OPTION_TEAM, GameService.NO_TEAM_ID));
      playerOptions.put(playerId, ImmutableMap.of(GameService.OPTION_FACTION, hostFaction));
      playerOptions.put(playerId, ImmutableMap.of(GameService.OPTION_START_SPOT, 1));
      playerOptions.put(playerId, ImmutableMap.of(GameService.OPTION_COLOR, 1));
      playerOptions.put(playerId, ImmutableMap.of(GameService.OPTION_ARMY, 2));

      int opponentId = opponent.getId();
      playerOptions.put(opponentId, ImmutableMap.of(GameService.OPTION_TEAM, GameService.NO_TEAM_ID));
      playerOptions.put(opponentId, ImmutableMap.of(GameService.OPTION_FACTION, opponentFaction));
      playerOptions.put(opponentId, ImmutableMap.of(GameService.OPTION_START_SPOT, 2));
      playerOptions.put(opponentId, ImmutableMap.of(GameService.OPTION_COLOR, 2));
      playerOptions.put(opponentId, ImmutableMap.of(GameService.OPTION_ARMY, 3));

      gameService.joinGame(game.getId(), opponent);
    });
  }

  /**
   * Checks whether the specified match passes the minimum required game quality. The longer {@code match} has been
   * around, the lower the required game quality in order to pass.
   */
  private boolean passesMinimumQuality(Match match) {
    double desiredGameQuality = properties.getMatchMaker().getDesiredGameQuality();
    double acceptableGameQuality = properties.getMatchMaker().getAcceptableGameQuality();
    long acceptableQualityWaitTime = Math.max(1, properties.getMatchMaker().getAcceptableQualityWaitTime());

    long secondsPassed = Math.max(1, Duration.between(match.leftSearch.createdTime, Instant.now()).getSeconds());

    float reductionPercent = secondsPassed / acceptableQualityWaitTime;
    float reduction = (float) (reductionPercent * (desiredGameQuality - acceptableGameQuality));

    return match.quality > desiredGameQuality - reduction;
  }

  private boolean isNotBanned(Player player) {
    return player.getMatchMakerBanDetails() == null;
  }

  /**
   * For each submitted search, a {@code PotentialMatch} is calculated against every online player. If the resulting
   * quality is high enough, the affected player will be notified.
   */
  @RequiredArgsConstructor
  private static class PotentialMatch {
    private final Player rightPlayer;
    private final String queueName;
    private final double quality;
  }

  /**
   * Represents a match of two searches. A {@code Match} is only created if a minimum quality is met.
   */
  @RequiredArgsConstructor
  private static class Match {
    private final MatchMakerSearch leftSearch;
    private final MatchMakerSearch rightSearch;
    private final double quality;
  }

  /**
   * Represents a search submitted by a player. Two matching searches can result in a {@link Match}.
   */
  @RequiredArgsConstructor
  private static class MatchMakerSearch {
    private final Instant createdTime;
    private final Player player;
    private final Faction faction;
    private final String queueName;
  }
}
