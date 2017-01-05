package com.faforever.server.integration;

import com.faforever.server.response.GameResponse;
import com.faforever.server.game.GameLaunchMessage;
import com.faforever.server.game.GameService;
import com.faforever.server.request.HostGameRequest;
import com.faforever.server.request.JoinGameRequest;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.security.access.prepost.PreAuthorize;

@MessageEndpoint
public class FafMessageEndpoint {

  private final GameService gameService;

  public FafMessageEndpoint(GameService gameService) {
    this.gameService = gameService;
  }

  @ServiceActivator(inputChannel = ChannelNames.HOST_GAME_REQUEST)
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public GameResponse hostGameRequeset(HostGameRequest hostGameRequest) {
    return gameService.createGame(hostGameRequest);
  }

  @ServiceActivator(inputChannel = ChannelNames.JOIN_GAME)
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public GameLaunchMessage joinGameRequest(JoinGameRequest joinGameRequest) {
    return gameService.joinGame(joinGameRequest);
  }
}
