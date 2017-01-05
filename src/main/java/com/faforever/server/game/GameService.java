package com.faforever.server.game;

import com.faforever.server.response.GameResponse;
import com.faforever.server.request.HostGameRequest;
import com.faforever.server.request.JoinGameRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class GameService {

  private final GameRepository gameRepository;
  private final AtomicInteger gameId;

  public GameService(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
    gameId = new AtomicInteger();
  }

  @PostConstruct
  public void postConstruct() {
    gameRepository.findMaxId().ifPresent(gameId::set);
    log.debug("Last game ID was: {}", gameId.get());
  }

  // FIXME implement
  public GameResponse createGame(HostGameRequest hostGameRequest) {
    return new GameResponse(gameId.incrementAndGet());
  }

  public GameLaunchMessage joinGame(JoinGameRequest joinGameRequest) {
    return new GameLaunchMessage(joinGameRequest.getId(), "", Collections.emptyList());
  }
}
