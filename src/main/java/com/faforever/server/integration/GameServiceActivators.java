package com.faforever.server.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.game.*;
import com.faforever.server.integration.request.GameStateReport;
import com.faforever.server.integration.request.HostGameRequest;
import com.faforever.server.statistics.ArmyStatisticsReport;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;


@MessageEndpoint
public class GameServiceActivators {

  private final GameService gameService;

  public GameServiceActivators(GameService gameService) {
    this.gameService = gameService;
  }

  @ServiceActivator(inputChannel = ChannelNames.HOST_GAME_REQUEST)
  public void hostGameRequest(HostGameRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.createGame(request.getTitle(), resolveMod(request.getMod()), request.getMapName(), request.getPassword(),
      request.getVisibility(), clientConnection.getUserDetails().getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.JOIN_GAME_REQUEST)
  public void joinGameRequest(JoinGameRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.joinGame(request.getId(), clientConnection.getUserDetails().getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.UPDATE_GAME_STATE_REQUEST)
  public void updateGameState(GameStateReport report, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.updatePlayerGameState(report.getState(), clientConnection.getUserDetails().getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.GAME_OPTION_REQUEST)
  public void updateGameOption(GameOptionReport report, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.updateGameOption(clientConnection.getUserDetails().getPlayer(), report.getKey(), report.getValue());
  }

  @ServiceActivator(inputChannel = ChannelNames.PLAYER_OPTION_REQUEST)
  public void updatePlayerOption(PlayerOptionReport report, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.updatePlayerOption(clientConnection.getUserDetails().getPlayer(), report.getPlayerId(), report.getKey(), report.getValue());
  }

  @ServiceActivator(inputChannel = ChannelNames.CLEAR_SLOT_REQUEST)
  public void clearSlot(ClearSlotRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.clearSlot(clientConnection.getUserDetails().getPlayer().getCurrentGame(), request.getSlotId());
  }

  @ServiceActivator(inputChannel = ChannelNames.AI_OPTION_REQUEST)
  public void updateAiOption(AiOptionReport report, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.updateAiOption(clientConnection.getUserDetails().getPlayer(), report.getAiName(), report.getKey(), report.getValue());
  }

  @ServiceActivator(inputChannel = ChannelNames.DESYNC_REPORT)
  public void reportDesync(DesyncReport desyncReport, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.reportDesync(clientConnection.getUserDetails().getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.GAME_MODS_REPORT)
  public void updateGameMods(GameModsReport report, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.updateGameMods(clientConnection.getUserDetails().getPlayer().getCurrentGame(), report.getModUids());
  }

  @ServiceActivator(inputChannel = ChannelNames.GAME_MODS_COUNT_REPORT)
  public void updateGameModsCount(GameModsCountReport report, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.updateGameModsCount(clientConnection.getUserDetails().getPlayer().getCurrentGame(), report.getCount());
  }

  @ServiceActivator(inputChannel = ChannelNames.ARMY_OUTCOME_REPORT)
  public void reportArmyOutcome(ArmyOutcomeReport report, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.reportArmyOutcome(clientConnection.getUserDetails().getPlayer(), report.getArmyId(), report.getOutcome());
  }

  @ServiceActivator(inputChannel = ChannelNames.ARMY_SCORE_REPORT)
  public void reportArmyScore(ArmyScoreReport report, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.reportArmyScore(clientConnection.getUserDetails().getPlayer(), report.getArmyId(), report.getScore());
  }

  @ServiceActivator(inputChannel = ChannelNames.GAME_STATISTICS_REPORT)
  public void reportGameStatistics(ArmyStatisticsReport report, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.reportArmyStatistics(clientConnection.getUserDetails().getPlayer(), report.getArmyStatistics());
  }

  @ServiceActivator(inputChannel = ChannelNames.ENFORCE_RATING_REQUEST)
  public void enforceRating(@Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    gameService.enforceRating(clientConnection.getUserDetails().getPlayer());
  }

  private byte resolveMod(String mod) {
    // FIXME implement
    return 1;
  }
}
