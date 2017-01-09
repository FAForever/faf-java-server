package com.faforever.server.integration;

import com.faforever.server.entity.Player;
import com.faforever.server.game.*;
import com.faforever.server.integration.request.HostGameRequest;
import com.faforever.server.integration.request.JoinGameRequest;
import com.faforever.server.integration.request.UpdateGameStateRequest;
import com.faforever.server.client.ClientConnection;
import com.faforever.server.map.MapService;
import com.faforever.server.player.PlayerService;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

import static com.faforever.server.client.ClientConnection.CLIENT_CONNECTION;

@MessageEndpoint
public class GameServiceActivators {

  private final GameService gameService;

  public GameServiceActivators(GameService gameService) {
    this.gameService = gameService;
  }

  @ServiceActivator(inputChannel = ChannelNames.HOST_GAME_REQUEST)
  public void hostGameRequest(HostGameRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.createGame(request.getTitle(), resolveMod(request.getMod()), request.getMapName(), request.getPassword(), clientConnection.getUserDetails().getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.JOIN_GAME_REQUEST)
  public void joinGameRequest(JoinGameRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.joinGame(request.getId(), clientConnection.getUserDetails().getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.UPDATE_GAME_STATE_REQUEST)
  public void updateGameState(UpdateGameStateRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.updateGameState(request.getGameState(), clientConnection.getUserDetails().getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.GAME_OPTION_REQUEST)
  public void updateGameOption(GameOptionRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.updateGameOption(clientConnection.getUserDetails().getPlayer(), request.getKey(), request.getValue());
  }

  @ServiceActivator(inputChannel = ChannelNames.PLAYER_OPTION_REQUEST)
  public void updatePlayerOption(PlayerOptionRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.updatePlayerOption(clientConnection.getUserDetails().getPlayer(), request.getPlayerId(), request.getKey(), request.getValue());
  }

  @ServiceActivator(inputChannel = ChannelNames.CLEAR_SLOT_REQUEST)
  public void clearSlot(ClearSlotRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.clearSlot(clientConnection.getUserDetails().getPlayer().getCurrentGame(), request.getSlotId());
  }

  @ServiceActivator(inputChannel = ChannelNames.AI_OPTION_REQUEST)
  public void updateAiOption(AiOptionRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.updateAiOption(clientConnection.getUserDetails().getPlayer(), request.getAiName(), request.getKey(), request.getValue());
  }

  private byte resolveMod(String mod) {
    // FIXME implement
    return 1;
  }
}
