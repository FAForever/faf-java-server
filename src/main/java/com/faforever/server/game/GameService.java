package com.faforever.server.game;

import com.faforever.server.client.ClientService;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.entity.ArmyOutcome;
import com.faforever.server.entity.ArmyScore;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.GamePlayerStats;
import com.faforever.server.entity.GameState;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.Rankiness;
import com.faforever.server.entity.VictoryCondition;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.Requests;
import com.faforever.server.map.MapService;
import com.faforever.server.mod.ModService;
import com.faforever.server.rating.RatingService;
import com.faforever.server.statistics.ArmyStatistics;
import com.faforever.server.stats.ArmyStatisticsService;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GameService {

  static final String OPTION_FOG_OF_WAR = "FogOfWar";
  static final String OPTION_CHEATS_ENABLED = "CheatsEnabled";
  static final String OPTION_PREBUILT_UNITS = "PrebuiltUnits";
  static final String OPTION_NO_RUSH = "NoRushOption";
  static final String OPTION_RESTRICTED_CATEGORIES = "RestrictedCategories";
  static final String OPTION_SLOT = "Slot";
  static final String OPTION_SLOTS = "Slots";
  static final String OPTION_SCENARIO_FILE = "ScenarioFile";
  static final String OPTION_TITLE = "Title";
  static final String OPTION_TEAM = "Team";
  static final Duration DEFAULT_MIN_DELAY = Duration.ofSeconds(1);
  static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(5);
  /**
   * ID of the team that stands for "no team" according to the game.
   */
  private static final byte NO_TEAM_ID = 1;
  private final GameRepository gameRepository;
  private final AtomicInteger nextGameId;
  private final ClientService clientService;
  private final Map<Integer, Game> gamesById;
  private final MapService mapService;
  private final ModService modService;
  private final RatingService ratingService;
  private ArmyStatisticsService armyStatisticsService;

  public GameService(GameRepository gameRepository, ClientService clientService, MapService mapService,
                     ModService modService, RatingService ratingService, ArmyStatisticsService armyStatisticsService) {
    this.gameRepository = gameRepository;
    this.clientService = clientService;
    this.mapService = mapService;
    this.modService = modService;
    this.ratingService = ratingService;
    this.armyStatisticsService = armyStatisticsService;
    nextGameId = new AtomicInteger();
    gamesById = new ConcurrentHashMap<>();
  }

  @PostConstruct
  public void postConstruct() {
    gameRepository.findMaxId().ifPresent(nextGameId::set);
    log.debug("Next game ID is: {}", nextGameId.get());
  }

  /**
   * Creates a new, transient game with the specified options and tells the client to start the game process. The
   * player's current game is set to the new game.
   */
  public void createGame(String title, byte modId, String mapname, String password, GameVisibility visibility, Player player) {
    Requests.verify(player.getCurrentGame() == null, ErrorCode.ALREADY_IN_GAME);

    int gameId = this.nextGameId.getAndIncrement();
    Game game = new Game(gameId);
    game.setHost(player);
    modService.getFeaturedMod(modId).ifPresent(game::setFeaturedMod);
    game.setTitle(title);
    mapService.findMap(mapname).ifPresent(game::setMap);
    game.setMapName(mapname);
    game.setPassword(password);
    game.setGameVisibility(visibility);

    log.debug("Player '{}' creates game '{}'", player, game);

    gamesById.put(gameId, game);
    addPlayer(game, player);

    clientService.startGameProcess(game, player);
  }

  /**
   * Tells the client to start the game process and sets the player's current game to it.
   */
  public void joinGame(int gameId, Player player) {
    Requests.verify(player.getCurrentGame() == null, ErrorCode.ALREADY_IN_GAME);

    Game game = getGame(gameId).orElseThrow(() -> new IllegalArgumentException("No such game: " + gameId));
    Requests.verify(game.getState() == GameState.OPEN, ErrorCode.GAME_NOT_JOINABLE);

    log.debug("Player '{}' joins game '{}'", player, gameId);
    clientService.startGameProcess(game, player);
    addPlayer(game, player);
  }

  public void updatePlayerGameState(PlayerGameState newState, Player player) {
    Requests.verify(player.getCurrentGame() != null, ErrorCode.NOT_IN_A_GAME);

    log.debug("Player '{}' updated his game state from '{}' to '{}'", player, player.getGameState(), newState);
    player.setGameState(newState);

    // FIXME figure out how leaving/closing a game is detected and clean up player options and stats

    Game game = player.getCurrentGame();
    switch (newState) {
      case LOBBY:
        onLobbyEntered(player, game);
        break;
      case LAUNCHING:
        onGameLaunching(player, game);
        break;
      case ENDED:
        onPlayerGameEnded(player, game);
        break;
    }
  }

  public Optional<Game> getGame(int id) {
    return Optional.ofNullable(gamesById.get(id));
  }

  public void updateGameOption(Player host, String key, Object value) {
    Game game = host.getCurrentGame();
    if (game == null) {
      // Since this is called repeatedly, throwing exceptions here would not be a good idea
      log.debug("Received game option for player w/o game: {}", host);
      return;
    }
    Requests.verify(Objects.equals(host.getCurrentGame().getHost(), host), ErrorCode.HOST_ONLY_OPTION, key);

    log.trace("Updating game option for game '{}': '{}' = '{}'", game.getId(), key, value);
    game.getOptions().put(key, value);
    if (VictoryCondition.GAME_OPTION_NAME.equals(key)) {
      game.setVictoryCondition(VictoryCondition.fromString((String) value));
    } else if (OPTION_SLOTS.equals(key)) {
      game.setMaxPlayers((int) value);
    } else if (OPTION_SCENARIO_FILE.equals(value)) {
      game.setMapName(((String) value).replace("//", "/").replace("\\", "/").split("/")[2]);
    } else if (OPTION_TITLE.equals(value)) {
      game.setTitle((String) value);
    }
    markDirty(game, DEFAULT_MIN_DELAY, DEFAULT_MAX_DELAY);
  }

  public void updatePlayerOption(Player host, int playerId, String key, Object value) {
    Game game = host.getCurrentGame();
    if (game == null) {
      // Since this is called repeatedly, throwing exceptions here would not be a good idea. Happens after restarts.
      log.warn("Received player option for player w/o game: {}", host);
      return;
    }
    Requests.verify(Objects.equals(host.getCurrentGame().getHost(), host), ErrorCode.HOST_ONLY_OPTION, key);

    if (game.getPlayerStats().stream().noneMatch(gamePlayerStats -> gamePlayerStats.getPlayer().getId() == playerId)) {
      log.warn("Player '{}' reported options for unknown player '{}' in game '{}'", host, playerId);
    }

    log.trace("Updating option for player '{}' in game '{}': '{}' = '{}'", playerId, game.getId(), key, value);
    game.getPlayerOptions().computeIfAbsent(playerId, id -> new HashMap<>()).put(key, value);

    if (OPTION_TEAM.equals(key)) {
      Optional<GamePlayerStats> playerStats = game.getPlayerStats().stream()
        .filter(gamePlayerStats -> gamePlayerStats.getPlayer().getId() == playerId)
        .findFirst();
      if (!playerStats.isPresent()) {
        log.warn("Player '{}' reported team '{}' for nonexistent player '{}' in game '{}'", host, value, playerId, game);
      } else {
        playerStats.get().setTeam(((Integer) value).byteValue());
      }
    }

    markDirty(game, Duration.ofSeconds(1), Duration.ofSeconds(5));
  }

  public void updateAiOption(Player host, String aiName, String key, Object value) {
    Game game = host.getCurrentGame();
    if (game == null) {
      // Since this is called repeatedly, throwing exceptions here would not be a good idea. Happens after restarts.
      log.warn("Received AI option for player w/o game: {}", host);
      return;
    }
    Requests.verify(Objects.equals(host.getCurrentGame().getHost(), host), ErrorCode.HOST_ONLY_OPTION, key);

    log.trace("Updating option for AI '{}' in game '{}': '{}' = '{}'", aiName, game.getId(), key, value);
    game.getAiOptions().computeIfAbsent(aiName, s -> new HashMap<>()).put(key, value);
    markDirty(game, DEFAULT_MIN_DELAY, DEFAULT_MAX_DELAY);
  }

  /**
   * Removes all player or AI options that are associated with the specified slot.
   */
  public void clearSlot(Game game, int slotId) {
    log.trace("Clearing slot '{}' of game '{}'", game, slotId);

    game.getPlayerOptions().entrySet().stream()
      .filter(entry -> Objects.equals(entry.getValue().get(OPTION_SLOT), slotId))
      .map(Entry::getKey)
      .collect(Collectors.toList())
      .forEach(playerId -> {
        log.trace("Removing options for player '{}' in game '{}'", playerId, game);
        game.getPlayerOptions().remove(playerId);
      });

    game.getAiOptions().entrySet().stream()
      .filter(entry -> Objects.equals(entry.getValue().get(OPTION_SLOT), slotId))
      .map(Entry::getKey)
      .collect(Collectors.toList())
      .forEach(aiName -> {
        log.trace("Removing options for AI '{}' in game '{}'", aiName, game);
        game.getAiOptions().remove(aiName);
      });

    markDirty(game, DEFAULT_MIN_DELAY, DEFAULT_MAX_DELAY);
  }

  /**
   * Increments the desync counter for the player's game.
   */
  public void reportDesync(Player player) {
    if (player.getCurrentGame() == null) {
      log.warn("Desync reported by player w/o game: {}", player);
      return;
    }
    int desyncCount = player.getCurrentGame().getDesyncCounter().incrementAndGet();
    log.debug("Player '{}' increased desync count to '{}' for game: {}", desyncCount, player.getCurrentGame());
  }

  /**
   * Updates the list of activated mod UIDs. Not all UIDs may be known to the server.
   */
  public void updateGameMods(Game game, List<String> modUids) {
    modService.getMods(modUids);
    // TODO lookup mod names
    game.setSimMods(modUids);
    markDirty(game, DEFAULT_MIN_DELAY, DEFAULT_MAX_DELAY);
  }

  public void updateGameModsCount(Game game, int count) {
    if (count != 0) {
      return;
    }
    log.trace("Clearing mod list for game '{}'", game);
    game.setSimMods(Collections.emptyList());
    markDirty(game, DEFAULT_MIN_DELAY, DEFAULT_MAX_DELAY);
  }

  public void reportArmyScore(Player player, int armyId, int score) {
    Game game = player.getCurrentGame();
    if (game == null) {
      log.warn("Army result reported by player w/o game: {}", player);
      return;
    }

    Optional<Integer> army = findArmy(armyId, game);
    if (!army.isPresent()) {
      log.warn("Player '{}' reported score '{}' for unknown army '{}' in game '{}'", player, score, armyId, game);
      return;
    }

    log.debug("Player '{}' reported result for army '{}' in game '{}': {}", player, armyId, game, score);
    game.getReportedArmyScores().computeIfAbsent(player.getId(), playerId -> new ArrayList<>()).add(new ArmyScore(armyId, score));
  }

  public void reportArmyOutcome(Player player, int armyId, Outcome outcome) {
    Game game = player.getCurrentGame();
    if (game == null) {
      log.warn("Army score reported by player w/o game: {}", player);
      return;
    }

    Optional<Integer> army = findArmy(armyId, game);
    if (!army.isPresent()) {
      log.warn("Player '{}' reported outcome '{}' for unknown army '{}' in game '{}'", player, outcome, armyId, game);
      return;
    }

    log.debug("Player '{}' reported result for army '{}' in game '{}': {}", player, armyId, game, outcome);
    game.getReportedArmyOutcomes().computeIfAbsent(player.getId(), playerId -> new ArrayList<>()).add(new ArmyOutcome(armyId, outcome));
  }

  /**
   * Updates the game's army statistics. Last reporter wins.
   */
  public void reportArmyStatistics(Player player, List<ArmyStatistics> armyStatistics) {
    Game game = player.getCurrentGame();
    if (game == null) {
      log.warn("Game statistics reported by player w/o game: {}", player);
      return;
    }
    game.replaceArmyStatistics(armyStatistics);
  }

  /**
   * Enforce rating even though the minimum game time has not yet been reached.
   */
  public void enforceRating(Player player) {
    Game game = player.getCurrentGame();
    if (game == null) {
      log.warn("Game statistics reported by player w/o game: {}", player);
      return;
    }
    log.debug("Player '{}' enforced rating for game '{}'", player, game);
    game.setRatingEnforced(true);
  }

  @EventListener
  public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
    clientService.sendGameList(getActiveGames(), (ConnectionAware) event.getAuthentication().getDetails());
  }

  private void addPlayer(Game game, Player player) {
    GamePlayerStats gamePlayerStats = new GamePlayerStats();
    Optional.ofNullable(player.getGlobalRating()).ifPresent(globalRating -> {
      gamePlayerStats.setDeviation(globalRating.getDeviation());
      gamePlayerStats.setMean(globalRating.getMean());
    });
    if (modService.isLadder1v1(game.getFeaturedMod())) {
      Optional.ofNullable(player.getLadder1v1Rating()).ifPresent(ladder1v1Rating -> {
        gamePlayerStats.setDeviation(ladder1v1Rating.getDeviation());
        gamePlayerStats.setMean(ladder1v1Rating.getMean());
      });
    }
    gamePlayerStats.setPlayer(player);
    game.getPlayerStats().add(gamePlayerStats);
    game.getActivePlayers().put(player.getId(), player);
    player.setCurrentGame(game);

    markDirty(game, DEFAULT_MIN_DELAY, DEFAULT_MAX_DELAY);
  }

  private void removePlayer(Game game, Player player) {
    player.setCurrentGame(game);
    game.getActivePlayers().remove(player.getId(), player);
  }

  private void onPlayerGameEnded(Player reporter, Game game) {
    if (game == null) {
      // Since this is called repeatedly, throwing exceptions here would not be a good idea. Happens after restarts.
      log.warn("Received player option for player w/o game: {}", reporter);
      return;
    }

    log.debug("Player '{}' left game: {}", reporter, game);
    removePlayer(game, reporter);

    if (game.getActivePlayers().isEmpty()) {
      onGameEnded(game);
    }
  }

  private void onGameEnded(Game game) {
    log.debug("Game ended: {}", game);
    game.getPlayerStats().forEach(stats -> {
      Player player = stats.getPlayer();
      armyStatisticsService.process(player, game, game.getArmyStatistics());
    });

    game.setState(GameState.CLOSED);
    updateRatingsIfValid(game);
    markDirty(game, Duration.ZERO, Duration.ZERO);
  }

  private void updateRatingsIfValid(Game game) {
    if (game.getRankiness() != Rankiness.RANKED) {
      return;
    }
    ratingService.updateRatings(game.getPlayerStats(), NO_TEAM_ID);
  }

  private void onGameLaunching(Player reporter, Game game) {
    if (!Objects.equals(game.getHost(), reporter)) {
      // TODO do non-hosts send this? If not, log to WARN
      log.trace("Player '{}' reported launch for game: {}", reporter, game);
      return;
    }
    game.setState(GameState.PLAYING);
    game.setLaunchedAt(Instant.now());
    updateGameRankiness(game);
    gameRepository.save(game);
    log.debug("Game launched: {}", game);
    markDirty(game, Duration.ZERO, Duration.ZERO);
  }

  private Duration duration(Game game) {
    return Duration.between(game.getLaunchedAt(), Instant.now());
  }

  /**
   * <p>Called when a player's game entered {@link PlayerGameState#LOBBY}. If the player is host, the state of the {@link Game}
   * instance will be updated and the player is requested to "host" a game (open a port so others can connect).
   * A joining player whose game entered {@link PlayerGameState#LOBBY} will be told to connect to the host and any other
   * players in the game.</p>
   * <p>In any case, the player will be added to the game's transient list of participants where team information,
   * faction and color will be set. When the game starts, this list will be reduced to only the players who are in the
   * game and then persisted.</p>
   */
  private void onLobbyEntered(Player player, Game game) {
    if (Objects.equals(game.getHost(), player)) {
      game.setState(GameState.OPEN);
      clientService.hostGame(game, player);
      markDirty(game, DEFAULT_MIN_DELAY, DEFAULT_MAX_DELAY);
    } else {
      clientService.connectToHost(game, player);
      game.getActivePlayers().values().forEach(otherPlayer -> connectPeers(player, otherPlayer));
    }
  }

  private void connectPeers(Player player, Player otherPlayer) {
    if (player == otherPlayer) {
      log.warn("Player '{}' should not be told to connect to himself");
      return;
    }
    clientService.connectToPlayer(player, player);
  }

  /**
   * Returns a list of games which haven't finished yet.
   */
  private Collection<Game> getActiveGames() {
    return Collections.unmodifiableCollection(gamesById.values());
  }

  private Optional<Integer> findArmy(int armyId, Game game) {
    return game.getPlayerOptions().values().stream()
      .map(options -> (int) options.get("Army"))
      .filter(id -> id == armyId)
      .findFirst();
  }

  private void markDirty(Game game, Duration minDelay, Duration maxDelay) {
    clientService.submitDirty(game, minDelay, maxDelay, Game::getId, GameResponse::new);
  }

  private boolean isFreeForAll(Game game) {
    if (game.getPlayerStats().size() < 3) {
      return false;
    }
    Set<Integer> teams = new HashSet<>();
    for (GamePlayerStats stats : game.getPlayerStats()) {
      int team = stats.getTeam();
      if (team != NO_TEAM_ID) {
        if (teams.contains(team)) {
          return false;
        }
        teams.add(team);
      }
    }
    return true;
  }

  /**
   * Checks the game settings and determines whether the game is ranked. If the game is unranked, its "rankiness"
   * will be updated
   */
  @VisibleForTesting
  void updateGameRankiness(Game game) {
    if (game.getRankiness() != Rankiness.RANKED) {
      throw new IllegalStateException("Rankiness has already been set");
    }
    if (!game.getSimMods().stream().allMatch(modService::isModRanked)) {
      game.setRankiness(Rankiness.BAD_MOD);
    } else if (game.getVictoryCondition() != VictoryCondition.DEMORALIZATION && !modService.isCoop(game.getFeaturedMod())) {
      game.setRankiness(Rankiness.WRONG_VICTORY_CONDITION);
    } else if (isFreeForAll(game)) {
      game.setRankiness(Rankiness.FREE_FOR_ALL);
    } else if (!areTeamsEven(game)) {
      game.setRankiness(Rankiness.UNEVEN_TEAMS);
    } else if (!"explored".equals(game.getOptions().get(OPTION_FOG_OF_WAR))) {
      game.setRankiness(Rankiness.NO_FOG_OF_WAR);
    } else if (!"false".equals(game.getOptions().get(OPTION_CHEATS_ENABLED))) {
      game.setRankiness(Rankiness.CHEATS_ENABLED);
    } else if (!"Off".equals(game.getOptions().get(OPTION_PREBUILT_UNITS))) {
      game.setRankiness(Rankiness.PREBUILT_ENABLED);
    } else if (!"Off".equals(game.getOptions().get(OPTION_NO_RUSH))) {
      game.setRankiness(Rankiness.NO_RUSH_ENABLED);
    } else if ((int) game.getOptions().get(OPTION_RESTRICTED_CATEGORIES) != 0) {
      game.setRankiness(Rankiness.BAD_UNIT_RESTRICTIONS);
    } else if (game.getMap() == null || !game.getMap().isRanked()) {
      game.setRankiness(Rankiness.BAD_MAP);
    } else if (game.getDesyncCounter().intValue() > game.getPlayerStats().size()) {
      game.setRankiness(Rankiness.TOO_MANY_DESYNCS);
    } else if (game.isMutuallyAgreedDraw()) {
      game.setRankiness(Rankiness.MUTUAL_DRAW);
    } else if (game.getPlayerStats().size() < 2) {
      game.setRankiness(Rankiness.SINGLE_PLAYER);
    } else if (game.getReportedArmyOutcomes().isEmpty() || game.getReportedArmyScores().isEmpty()) {
      game.setRankiness(Rankiness.UNKNOWN_RESULT);
    }
  }

  @VisibleForTesting
  boolean areTeamsEven(Game game) {
    Map<Integer, Long> playersPerTeam = game.getPlayerStats().stream()
      .map(GamePlayerStats::getTeam)
      .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    if (playersPerTeam.containsKey(NO_TEAM_ID)) {
      // There are players without a team, all other teams must have exactly 1 player
      return playersPerTeam.entrySet().stream()
        .filter(teamToCount -> teamToCount.getKey() != NO_TEAM_ID)
        .allMatch(teamToCount -> teamToCount.getValue() == 1);
    }
    // All teams must have the same amount of players
    return playersPerTeam.values().stream().distinct().count() == 1;
  }
}
