package com.faforever.server.integration;

import com.faforever.server.game.GameService;
import com.faforever.server.integration.request.HostGameRequest;
import com.faforever.server.integration.request.JoinGameRequest;
import com.faforever.server.integration.request.UpdateGameStateRequest;
import com.faforever.server.integration.session.ClientConnection;
import com.faforever.server.integration.session.SessionManager;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

import static com.faforever.server.integration.session.ClientConnection.CLIENT_CONNECTION;

@MessageEndpoint
public class GameServiceActivators {

  private final GameService gameService;

  public GameServiceActivators(GameService gameService, SessionManager sessionManager) {
    this.gameService = gameService;
  }

  @ServiceActivator(inputChannel = ChannelNames.HOST_GAME_REQUEST, outputChannel = ChannelNames.CLIENT_OUTBOUND)
  public void hostGameRequest(HostGameRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.createGame(request.getMod(), clientConnection.getUserDetails().getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.JOIN_GAME_REQUEST, outputChannel = ChannelNames.CLIENT_OUTBOUND)
  public void joinGameRequest(JoinGameRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.joinGame(request.getId(), clientConnection.getUserDetails().getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.UPDATE_GAME_STATE_REQUEST, outputChannel = ChannelNames.CLIENT_OUTBOUND)
  public void updateGameState(UpdateGameStateRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.updateGameState(request.getGameState(), clientConnection.getUserDetails().getPlayer());
  }
}
