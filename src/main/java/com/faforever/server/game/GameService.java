package com.faforever.server.game;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.GamePlayerStats;
import com.faforever.server.entity.Player;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.Requests;
import com.faforever.server.map.MapService;
import javafx.collections.FXCollections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class GameService {

  private final GameRepository gameRepository;
  private final AtomicInteger nextGameId;
  private final ClientService clientService;
  private final Map<Integer, Game> games;
  private final MapService mapService;
  private byte ladder1v1FeaturedModId;

  public GameService(GameRepository gameRepository, ClientService clientService, MapService mapService) {
    this.gameRepository = gameRepository;
    this.clientService = clientService;
    this.mapService = mapService;
    nextGameId = new AtomicInteger();
    games = FXCollections.observableMap(new ConcurrentHashMap<>());
  }

  @PostConstruct
  public void postConstruct() {
    gameRepository.findMaxId().ifPresent(nextGameId::set);
    log.debug("Next game ID is: {}", nextGameId.get());

    // FIXME read from database
    ladder1v1FeaturedModId = 1;
    log.debug("Ladder1v1 mod ID is: {}", ladder1v1FeaturedModId);
  }

  /**
   * Creates a new, transient game with the specified options and tells the client to start the game process. The
   * player's current game is set to the new game.
   */
  public void createGame(String title, byte modId, String mapname, String password, Player player) {
    Requests.verify(player.getCurrentGame() == null, ErrorCode.ALREADY_IN_GAME);

    int gameId = this.nextGameId.getAndIncrement();
    Game game = new Game(gameId);
    game.setHost(player);
    game.setGameMod(modId);
    game.setGameName(title);
    mapService.findMap(mapname).ifPresent(game::setMap);
    game.setMapName(mapname);
    game.setPassword(password);

    log.debug("Player '{}' creates game '{}'", player, game);

    games.put(gameId, game);
    player.setCurrentGame(game);

    clientService.startGameProcess(game, player);
  }

  /**
   * Tells the client to start the game process and sets the player's current game to it.
   */
  public void joinGame(int gameId, Player player) {
    Requests.verify(player.getCurrentGame() == null, ErrorCode.ALREADY_IN_GAME);

    getGame(gameId).map(game -> {
      log.debug("Player '{}' joins game '{}'", player, gameId);
      player.setCurrentGame(game);
      clientService.startGameProcess(game, player);
      return game;
    }).orElseThrow(() -> new IllegalArgumentException("No such game: " + gameId));
  }

  public void updateGameState(GameState newGameState, Player player) {
    Requests.verify(player.getCurrentGame() != null, ErrorCode.NOT_IN_A_GAME);

    log.debug("Player '{}' updated his game state from '{}' to '{}'", player, player.getGameState(), newGameState);
    player.setGameState(newGameState);

    Game game = player.getCurrentGame();
    if (newGameState == GameState.LOBBY) {
      handleLobbyState(player, game);
    } else if (newGameState == GameState.LAUNCHING) {
      log.debug("Persisting game: {}", game);
      gameRepository.save(game);
    } else if (newGameState == GameState.ENDED) {
      player.setCurrentGame(null);
    }
  }

  public Optional<Game> getGame(int id) {
    return Optional.ofNullable(games.get(id));
  }

  public void updateGameOption(Player host, String key, Object value) {
    Game game = host.getCurrentGame();
    if (game == null) {
      // Since this is called repeatedly, throwing exceptions here would not be a good idea
      log.debug("Received game option for player w/o game: {}", host);
    } else {
      log.trace("Updating game option for game '{}': '{}' = '{}'", game.getId(), key, value);
      game.getOptions().put(key, value);
    }
  }

  public void updatePlayerOption(Player host, int playerId, String key, Object value) {
    Game game = host.getCurrentGame();
    if (game == null) {
      // Since this is called repeatedly, throwing exceptions here would not be a good idea. Happens after restarts.
      log.warn("Received player option for player w/o game: {}", host);
    } else {
      log.trace("Updating option for player '{}' in game '{}': '{}' = '{}'", playerId, game.getId(), key, value);
      game.getPlayerOptions().get(playerId).put(key, value);
    }
  }

  public void updateAiOption(Player host, String aiName, String key, Object value) {
    Game game = host.getCurrentGame();
    if (game == null) {
      // Since this is called repeatedly, throwing exceptions here would not be a good idea. Happens after restarts.
      log.warn("Received AI option for player w/o game: {}", host);
    } else {
      log.trace("Updating option for AI '{}' in game '{}': '{}' = '{}'", aiName, game.getId(), key, value);
      game.getAiOptions().computeIfAbsent(aiName, s -> new HashMap<>()).put(key, value);
    }
  }

  /**
   * <p>Called when a player's game entered {@link GameState#LOBBY}. If the player is host, the state of the {@link Game}
   * instance will be updated and the player is requested to "host" a game (open a port so others can connect).
   * A joining player whose game entered {@link GameState#LOBBY} will be told to connect to the host and any other
   * players in the game.</p>
   * <p>In any case, the player will be added to the game's transient list of participants where team information,
   * faction and color will be set. This list will then be persisted as soon as the game starts.</p>
   */
  private void handleLobbyState(Player player, Game game) {
    if (Objects.equals(game.getHost(), player)) {
      verifyTransition(game.getGameState(), GameState.LOBBY);
      game.setGameState(GameState.LOBBY);
      clientService.hostGame(game, player);
    } else {
      log.debug("Telling '{}' to connect to '{}'", game.getHost());
      clientService.connectToHost(game, player);
    }

    GamePlayerStats gamePlayerStats = new GamePlayerStats();
    Optional.ofNullable(player.getGlobalRating()).ifPresent(globalRating -> {
      gamePlayerStats.setDeviation(globalRating.getDeviation());
      gamePlayerStats.setMean(globalRating.getMean());
    });
    if (isLadder1v1(game)) {
      Optional.ofNullable(player.getLadder1v1Rating()).ifPresent(ladder1v1Rating -> {
        gamePlayerStats.setDeviation(ladder1v1Rating.getDeviation());
        gamePlayerStats.setMean(ladder1v1Rating.getMean());
      });
    }
    gamePlayerStats.setPlayer(player);
    game.getPlayerStats().add(gamePlayerStats);
    game.getPlayerOptions().put(player.getId(), new HashMap<>());
  }

  /**
   * Checks whether a game is allowed to transition from an old game state into a new game state. If not, an
   * {@link IllegalStateException} will be thrown.
   */
  private static void verifyTransition(GameState oldState, GameState newState) {
    switch (newState) {
      case LOBBY:
        Assert.state(oldState == null, "Can't transition from " + oldState + " to " + newState);
        break;
      case PLAYING:
        Assert.state(oldState == GameState.PLAYING, "Can't transition from " + oldState + " to " + newState);
        break;
    }
  }

  /**
   * A poor man's solution to clear slots in case of "bugged" game states. Since there is no scenario - in a non-buggy
   * server software - where this can happen this method does nothing but log the action.
   *
   * @deprecated "clearSlots" should be removed from the game, so this method can be removed as well
   */
  @Deprecated
  public void clearSlot(Game game, int slotId) {
    log.trace("Ignoring clearSlot for game '{}' and ID '{}'", game, slotId);
  }

  private boolean isLadder1v1(Game game) {
    return game.getGameMod() == ladder1v1FeaturedModId;
  }
}
