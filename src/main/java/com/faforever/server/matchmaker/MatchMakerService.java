package com.faforever.server.matchmaker;

import com.faforever.server.client.ClientService;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.Ladder1v1Rating;
import com.faforever.server.entity.MapVersion;
import com.faforever.server.entity.Player;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.RequestException;
import com.faforever.server.error.Requests;
import com.faforever.server.game.Faction;
import com.faforever.server.game.GameService;
import com.faforever.server.game.GameVisibility;
import com.faforever.server.map.MapService;
import com.faforever.server.mod.ModService;
import com.faforever.server.player.PlayerService;
import com.faforever.server.rating.RatingService;
import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Holds multiple match maker pools to which searches can be submitted. Once at least two searches are available in the
 * same pool, the service will calculate the rating between the two players. If a configured minimum quality is met, the
 * two players will be matched. The minimum quality required for a match will be reduced the longer a search remains in
 * the queue until a configured lowest value is reached.
 */
@Slf4j
@Service
public class MatchMakerService {

  private static final double DESIRED_MIN_GAME_QUALITY = 0.8d;
  private static final String LADDER_1V1_MOD_NAME = "ladder1v1";
  private final ModService modService;
  private final ServerProperties properties;
  private final RatingService ratingService;
  /**
   * Maps pool name -&gt; player ID -&gt; search.
   */
  private final Map<String, Map<Integer, MatchMakerSearch>> searchesByPoolName;
  private final GameService gameService;
  private final MapService mapService;
  private final PlayerService playerService;
  private final ClientService clientService;
  private final Map<String, FeaturedMod> modByPoolName;

  public MatchMakerService(ModService modService, ServerProperties properties, RatingService ratingService,
                           ClientService clientService, GameService gameService, MapService mapService,
                           PlayerService playerService) {
    this.modService = modService;
    this.properties = properties;
    this.ratingService = ratingService;
    this.clientService = clientService;
    this.gameService = gameService;
    this.mapService = mapService;
    this.playerService = playerService;
    modByPoolName = new HashMap<>();
    searchesByPoolName = new HashMap<>();
  }

  /**
   * Submits a new search to the matchmaker, thus marking a player available for matchmaking. Any other searches by the
   * same player for the same pool will be updated.
   *
   * @param faction the faction the player will play once the game starts
   */
  public void submitSearch(Player player, Faction faction, String poolName) {
    modByPoolName.computeIfAbsent(LADDER_1V1_MOD_NAME, s -> modService.getLadder1v1().orElse(null));

    Requests.verify(player.getCurrentGame() == null, ErrorCode.ALREADY_IN_GAME);
    Requests.verify(isNotBanned(player), ErrorCode.BANNED_FROM_MATCH_MAKER);
    Requests.verify(modByPoolName.get(poolName) != null, ErrorCode.MATCH_MAKER_POOL_DOESNT_EXIST, poolName);

    Map<Integer, MatchMakerSearch> pool = getPool(poolName);

    synchronized (searchesByPoolName) {
      MatchMakerSearch search = getPool(poolName).get(player.getId());
      if (search != null) {
        log.debug("Updating search of player '{}' for pool '{}'", search.player, search.poolName);
        search.faction = faction;
      } else {
        log.debug("Adding player '{}' to pool '{}'", player, poolName);
        search = new MatchMakerSearch(Instant.now(), player, poolName, faction);
        pool.put(player.getId(), search);
      }
      notifyPlayers(search, DESIRED_MIN_GAME_QUALITY);
    }
  }

  /**
   * Processes all pools and removes pools that are empty afterwards.
   */
  @Scheduled(fixedDelay = 3000)
  public void processPools() {
    synchronized (searchesByPoolName) {
      searchesByPoolName.entrySet().stream()
        .peek(entry -> processPool(entry.getKey(), entry.getValue()))
        .filter(entry -> entry.getValue().isEmpty())
        .collect(Collectors.toList())
        .forEach(entry -> {
          log.debug("Removing empty pool '{}'", entry.getKey());
          searchesByPoolName.remove(entry.getKey());
        });
    }
  }

  /**
   * Removes the specified player from the specified pool.
   */
  public void cancelSearch(String poolName, Player player) {
    synchronized (searchesByPoolName) {
      log.debug("Removing searches from pool '{}' for player '{}'", poolName, player);
      Map<Integer, MatchMakerSearch> pool = getPool(poolName);
      pool.values().stream()
        .filter(search -> search.player.equals(player))
        .collect(Collectors.toList())
        .forEach(search -> pool.remove(search.player.getId()));
    }
  }

  public void removePlayer(Player player) {
    log.debug("Removing player '{}' from all pools", player);
    synchronized (searchesByPoolName) {
      searchesByPoolName.values().forEach(pool -> pool.remove(player.getId()));
    }
  }

  /**
   * <p>Creates a new match with the specified options and participants. All participants must be online and available
   * for matchmaking. A player can be unavailable for matchmaking if, for instance, he's currently playing a game or
   * offline. In this case, a {@link ErrorCode#PLAYER_NOT_AVAILABLE_FOR_MATCHMAKING} is thrown.</p>
   *
   * @throws RequestException if a player is not available for matchmaking or the map to be played is unknown by the
   * server.
   */
  public void createMatch(ConnectionAware requester, UUID requestId, String title, String featuredMod, List<MatchParticipant> participants, int mapVersionId) {
    log.debug("Creating match '{}' with '{}' participants on map '{}'", title, participants.size(), mapVersionId);

    Requests.verify(participants.size() > 1, ErrorCode.INSUFFICIENT_MATCH_PARTICIPANTS, participants.size(), 2);

    List<Player> players = participants.stream()
      .map(matchParticipant -> playerService.getOnlinePlayer(matchParticipant.getId())
        .orElseThrow(() -> new RequestException(requestId, ErrorCode.PLAYER_NOT_AVAILABLE_FOR_MATCHMAKING, matchParticipant.getId())))
      .peek(player -> Requests.verify(player.getCurrentGame() == null, requestId, ErrorCode.PLAYER_NOT_AVAILABLE_FOR_MATCHMAKING, player))
      .peek(this::removePlayer)
      .collect(Collectors.toList());

    String mapFileName = mapService.findMap(mapVersionId)
      .map(MapVersion::getFilename)
      .orElseThrow(() -> new RequestException(requestId, ErrorCode.UNKNOWN_MAP, mapVersionId));

    Player host = players.get(0);
    List<Player> guests = players.subList(1, players.size());

    gameService.createGame(title, featuredMod, mapFileName, null, GameVisibility.PRIVATE, null, null, host)
      .handle((game, throwable) -> {
        if (throwable != null) {
          log.debug("The host of match '{}' failed to start his game", title, throwable);
          throw new RequestException(requestId, ErrorCode.HOST_FAILED_TO_START_GAME, title, host);
        }

        AtomicInteger counter = new AtomicInteger();
        Integer hostId = host.getId();

        setPlayerOptionsForMatchParticipant(participants, host, counter, hostId);

        log.trace("Host '{}' for match '{}' is ready", host, title);
        clientService.sendMatchCreatedNotification(requestId, game.getId(), requester);

        List<CompletableFuture<Game>> guestGameFutures = guests.stream()
          .peek(player -> log.trace("Telling player '{}' to start the game process for match '{}'", player, title))
          .map(player -> gameService.joinGame(game.getId(), null, player)
            .thenApply(gameStartedFuture -> {
              setPlayerOptionsForMatchParticipant(participants, host, counter, player.getId());
              return gameStartedFuture;
            })
          )
          .collect(Collectors.toList());

        return CompletableFuture.allOf(guestGameFutures.toArray(new CompletableFuture[guests.size()]))
          .thenAccept(aVoid -> log.debug("All players launched their game for match '{}'", title))
          .exceptionally(throwable1 -> {
            log.debug("At least one player failed to launch their game for match '{}'", title, throwable1);
            return null;
          });
      });
  }

  private void setPlayerOptionsForMatchParticipant(List<MatchParticipant> participants, Player host, AtomicInteger counter, Integer playerId) {
    MatchParticipant participant = getMatchParticipant(participants, playerId);
    gameService.updatePlayerOption(host, playerId, GameService.OPTION_TEAM, participant.getTeam());
    gameService.updatePlayerOption(host, playerId, GameService.OPTION_FACTION, participant.getFaction().toFaValue());
    gameService.updatePlayerOption(host, playerId, GameService.OPTION_START_SPOT, participant.getStartSpot());
    gameService.updatePlayerOption(host, playerId, GameService.OPTION_COLOR, counter.incrementAndGet());
    // TODO check if enumerating armies makes sense or if the requester needs to specify
    gameService.updatePlayerOption(host, playerId, GameService.OPTION_ARMY, counter.get());
  }

  @NotNull
  private MatchParticipant getMatchParticipant(List<MatchParticipant> participants, int playerId) {
    return participants.stream()
      .filter(matchParticipant -> matchParticipant.getId() == playerId)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No match participant for player: " + playerId));
  }

  private void processPool(String poolName, Map<Integer, MatchMakerSearch> pool) {
    Set<MatchMakerSearch> processedSearches = new HashSet<>();

    log.trace("Processing '{}' entries of pool '{}'", pool.size(), poolName);
    pool.values().stream()
      .sorted(Comparator.comparingInt(search -> search.player.getId()))
      .map((search) -> {
        processedSearches.add(search);
        return findMatch(search, processedSearches);
      })
      .filter(Optional::isPresent)
      .forEach(optional -> {
        Match match = optional.get();
        MatchMakerSearch leftSearch = match.leftSearch;
        MatchMakerSearch rightSearch = match.rightSearch;

        Player leftPlayer = leftSearch.player;
        Player rightPlayer = rightSearch.player;

        log.debug("Player '{}' was matched against '{}' with game quality '{}'", leftPlayer, rightPlayer, match.quality);
        pool.remove(leftSearch.player.getId());
        pool.remove(rightSearch.player.getId());

        String technicalModName = modByPoolName.get(leftSearch.poolName).getTechnicalName();
        startGame(technicalModName, leftPlayer, leftSearch.faction, rightPlayer, rightSearch.faction);
      });
  }

  private Map<Integer, MatchMakerSearch> getPool(String poolName) {
    return searchesByPoolName.computeIfAbsent(poolName, s -> new HashMap<>());
  }

  /**
   * Tries to find the best match for a {@code search}. The minimum quality required for a match depends on the outcome
   * of {@link #passesMinimumQuality(Match)}.
   */
  private Optional<Match> findMatch(MatchMakerSearch leftSearch, Set<MatchMakerSearch> alreadyChecked) {
    Ladder1v1Rating defaultRating = (Ladder1v1Rating) new Ladder1v1Rating()
      .setMean(properties.getTrueSkill().getInitialStandardDeviation())
      .setDeviation(properties.getTrueSkill().getInitialStandardDeviation());

    String poolName = leftSearch.poolName;
    synchronized (searchesByPoolName) {
      return getPool(poolName).values().stream()
        .filter(otherSearch -> !alreadyChecked.contains(otherSearch))
        .map(rightSearch -> {
          alreadyChecked.add(rightSearch);
          return new Match(
            leftSearch,
            rightSearch,
            ratingService.calculateQuality(
              Optional.ofNullable(leftSearch.player.getLadder1v1Rating()).orElse(defaultRating),
              Optional.ofNullable(rightSearch.player.getLadder1v1Rating()).orElse(defaultRating)
            ));
        })
        .filter(this::passesMinimumQuality)
        .max(Comparator.comparingDouble(value -> value.quality));
    }
  }

  /**
   * Notify all players, except those already searching (which includes the search owner) about the {@code search},
   * given that they would result in a game with a quality of at least {@code minQuality}.
   */
  private void notifyPlayers(MatchMakerSearch search, double minQuality) {
    playerService.getPlayers().parallelStream()
      .filter(player -> !getPool(search.poolName).containsKey(player.getId()))
      .map(rightPlayer -> new PotentialMatch(
        rightPlayer,
        search.poolName,
        ratingService.calculateQuality(search.player.getLadder1v1Rating(), rightPlayer.getLadder1v1Rating())
      ))
      .filter(match -> match.quality > minQuality)
      .forEach(match -> clientService.sendMatchmakerNotification(match.poolName, match.rightPlayer));
  }

  /**
   * Tells one player to host a game and the other player to join the game.
   */
  private void startGame(String technicalModName, Player host, Faction hostFaction, Player opponent, Faction opponentFaction) {
    gameService.createGame(
      host.getLogin() + " vs. " + opponent.getLogin(),
      technicalModName,
      randomMap(),
      null,
      GameVisibility.PRIVATE,
      null,
      null,
      host
    )
      .thenCompose(game -> gameService.joinGame(game.getId(), null, opponent))
      .thenAccept(game -> {
        int hostId = host.getId();
        gameService.updatePlayerOption(host, hostId, GameService.OPTION_TEAM, GameService.NO_TEAM_ID);
        gameService.updatePlayerOption(host, hostId, GameService.OPTION_FACTION, hostFaction);
        gameService.updatePlayerOption(host, hostId, GameService.OPTION_START_SPOT, 1);
        gameService.updatePlayerOption(host, hostId, GameService.OPTION_COLOR, 1);
        gameService.updatePlayerOption(host, hostId, GameService.OPTION_ARMY, 2);

        int opponentId = opponent.getId();
        gameService.updatePlayerOption(host, opponentId, GameService.OPTION_TEAM, GameService.NO_TEAM_ID);
        gameService.updatePlayerOption(host, opponentId, GameService.OPTION_FACTION, opponentFaction);
        gameService.updatePlayerOption(host, opponentId, GameService.OPTION_START_SPOT, 2);
        gameService.updatePlayerOption(host, opponentId, GameService.OPTION_COLOR, 2);
        gameService.updatePlayerOption(host, opponentId, GameService.OPTION_ARMY, 3);
      });
  }

  private String randomMap() {
    return mapService.getRandomLadderMap().getFilename();
  }

  /**
   * Checks whether the specified match passes the minimum required game quality. The longer {@code match} has been
   * around, the lower the required game quality in order to pass.
   */
  private boolean passesMinimumQuality(Match match) {
    double desiredGameQuality = properties.getMatchMaker().getDesiredGameQuality();
    double acceptableGameQuality = properties.getMatchMaker().getAcceptableGameQuality();
    long acceptableQualityWaitTime = Math.max(1, properties.getMatchMaker().getAcceptableQualityWaitTime());

    long secondsPassed = Math.max(1, Duration.between(Instant.now(), match.leftSearch.createdTime).getSeconds());

    float reductionPercent = secondsPassed / acceptableQualityWaitTime;
    float reduction = (float) (reductionPercent * (desiredGameQuality - acceptableGameQuality));

    double requiredQuality = desiredGameQuality - reduction;
    boolean passes = match.quality > requiredQuality;
    log.trace("Calculated quality '{}' of required '{}' for match: {}", match);
    return passes;
  }

  private boolean isNotBanned(Player player) {
    return player.getMatchMakerBanDetails() == null;
  }

  /**
   * For each submitted search, a {@code PotentialMatch} is calculated against every online player. If the resulting
   * quality is high enough, the affected player will be notified.
   */
  @Value
  private static class PotentialMatch {
    Player rightPlayer;
    String poolName;
    double quality;
  }

  /**
   * Represents a match of two searches. A {@code Match} is only created if a minimum quality is met.
   */
  @Value
  private static class Match {
    MatchMakerSearch leftSearch;
    MatchMakerSearch rightSearch;
    double quality;
  }

  /**
   * Represents a search submitted by a player. Two matching searches can result in a {@link Match}.
   */
  @AllArgsConstructor
  @EqualsAndHashCode(of = {"poolName", "player"})
  private static class MatchMakerSearch {
    private final Instant createdTime;
    private final Player player;
    private final String poolName;
    private Faction faction;
  }

  @VisibleForTesting
  Map<String, Map<Integer, MatchMakerSearch>> getSearchPools() {
    return searchesByPoolName;
  }
}
