package com.faforever.server.integration;

import com.faforever.server.game.GameService;
import com.faforever.server.integration.request.HostGameRequest;
import com.faforever.server.integration.request.JoinGameRequest;
import com.faforever.server.integration.request.UpdateGameStateRequest;
import com.faforever.server.integration.response.GameLaunchResponse;
import com.faforever.server.integration.response.LaunchGameResponse;
import com.faforever.server.integration.session.Session;
import com.faforever.server.integration.session.SessionManager;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

import static com.faforever.server.integration.session.Session.SESSION;

@MessageEndpoint
public class GameServiceActivators {

  private final GameService gameService;

  public GameServiceActivators(GameService gameService, SessionManager sessionManager) {
    this.gameService = gameService;
  }

  @ServiceActivator(inputChannel = ChannelNames.HOST_GAME_REQUEST, outputChannel = ChannelNames.CLIENT_OUTBOUND)
  public LaunchGameResponse hostGameRequeset(HostGameRequest request, @Header(SESSION) Session session) {
    return gameService.createGame(request.getMod(), session.getUserDetails().getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.JOIN_GAME_REQUEST, outputChannel = ChannelNames.CLIENT_OUTBOUND)
  public GameLaunchResponse joinGameRequest(JoinGameRequest request, @Header(SESSION) Session session) {
    return gameService.joinGame(request.getId(), session.getUserDetails().getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.UPDATE_GAME_STATE_REQUEST, outputChannel = ChannelNames.CLIENT_OUTBOUND)
  public void updateGameState(UpdateGameStateRequest request, @Header(SESSION) Session session) {
    gameService.updateGameState(request.getGameId(), request.getGameState(), session.getUserDetails().getPlayer());
  }
}
