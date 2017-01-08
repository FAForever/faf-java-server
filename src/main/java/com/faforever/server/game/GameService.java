package com.faforever.server.game;

import com.faforever.server.entity.Game;
import com.faforever.server.entity.Player;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.RequestException;
import com.faforever.server.integration.ClientGateway;
import com.faforever.server.integration.response.LaunchGameResponse;
import javafx.collections.FXCollections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class GameService {

  private final GameRepository gameRepository;
  private final AtomicInteger nextGameId;
  private final ClientGateway clientGateway;
  private final Map<Integer, Game> games;

  public GameService(GameRepository gameRepository, ClientGateway clientGateway) {
    this.gameRepository = gameRepository;
    this.clientGateway = clientGateway;
    nextGameId = new AtomicInteger();
    games = FXCollections.observableMap(new ConcurrentHashMap<>());
  }

  @PostConstruct
  public void postConstruct() {
    gameRepository.findMaxId().ifPresent(nextGameId::set);
    log.debug("Next game ID is: {}", nextGameId.get());
  }

  public void createGame(String mod, Player player) {
    int gameId = this.nextGameId.getAndIncrement();
    Game game = new Game(gameId);
    log.debug("Player '{}' creates game '{}'", player, game);

    games.put(gameId, game);
    player.setCurrentGame(game);

    launchGame(mod, player, gameId);
  }

  public void joinGame(int gameId, Player player) {
    if (player.getCurrentGame() != null) {
      throw new RequestException(ErrorCode.ALREADY_IN_GAME);
    }

    getGame(gameId).map(game -> {
      log.debug("Player '{}' joins game '{}'", player, gameId);
      player.setCurrentGame(game);
      launchGame(resolveMod(game), player, gameId);
      return game;
    }).orElseThrow(() -> new IllegalArgumentException("No such game: " + gameId));
  }

  public void updateGameState(GameState newGameState, Player player) {
    if (player.getCurrentGame() == null) {
      throw new RequestException(ErrorCode.NOT_IN_A_GAME);
    }

    log.debug("Player '{}' updated game state from '{}' to '{}'", player, player.getGameState(), newGameState);
    player.setGameState(newGameState);

    Game game = player.getCurrentGame();
    if (Objects.equals(game.getHost(), player)) {
      verifyTransition(game.getGameState(), newGameState);
      game.setGameState(newGameState);
    } else {
      clientGateway.joinGame(game, player);
    }
  }

  public Optional<Game> getGame(int id) {
    return Optional.ofNullable(games.get(id));
  }

  private void launchGame(String mod, Player player, int gameId) {
    List<String> args = Arrays.asList("/numgames", String.valueOf(player.getGlobalRating().getNumGames()));
    clientGateway.launchGame(new LaunchGameResponse(mod, gameId, args), player);
  }

  private static void verifyTransition(GameState oldState, GameState newState) {
    switch (newState) {
      case LOBBY:
        Assert.state(oldState == GameState.IDLE, "Can't transition from " + oldState + " to " + newState);
        break;
      case PLAYING:
        Assert.state(oldState == GameState.PLAYING, "Can't transition from " + oldState + " to " + newState);
        break;
    }
  }

  private String resolveMod(Game game) {
    // FIXME implement
    return "faf";
  }
}
