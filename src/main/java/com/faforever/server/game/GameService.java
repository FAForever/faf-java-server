package com.faforever.server.game;

import com.faforever.server.entity.Game;
import com.faforever.server.entity.Player;
import com.faforever.server.integration.response.GameLaunchResponse;
import com.faforever.server.integration.response.LaunchGameResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class GameService {

  private final GameRepository gameRepository;
  private final ConcurrentHashMap<Integer, Game> games;
  private final AtomicInteger gameId;

  public GameService(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
    gameId = new AtomicInteger();
    games = new ConcurrentHashMap<>();
  }

  @PostConstruct
  public void postConstruct() {
    gameRepository.findMaxId().ifPresent(gameId::set);
    log.debug("Last game ID was: {}", gameId.get());
  }

  public LaunchGameResponse createGame(String mod, Player player) {
    short numGames = player.getGlobalRating().getNumGames();

    List<String> args = Arrays.asList("/numgames", String.valueOf(numGames));
    return new LaunchGameResponse(mod, gameId.incrementAndGet(), args);
  }

  public GameLaunchResponse joinGame(int gameId, Player player) {
    return new GameLaunchResponse(gameId, "", Collections.emptyList());
  }

  public void updateGameState(int gameId, GameState gameState, Player player) {
    if(!games.containsKey(gameId)){
      throw new IllegalArgumentException("No such game: " + gameId);
    }

    games.get(gameId).setGameState(gameState);
  }
}
