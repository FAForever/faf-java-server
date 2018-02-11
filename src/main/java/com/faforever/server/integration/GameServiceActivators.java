package com.faforever.server.integration;

import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.entity.Player;
import com.faforever.server.game.AiOptionReport;
import com.faforever.server.game.ArmyOutcomeReport;
import com.faforever.server.game.ArmyScoreReport;
import com.faforever.server.game.ClearSlotRequest;
import com.faforever.server.game.DesyncReport;
import com.faforever.server.game.DisconnectPeerRequest;
import com.faforever.server.game.GameModsCountReport;
import com.faforever.server.game.GameModsReport;
import com.faforever.server.game.GameOptionReport;
import com.faforever.server.game.GameService;
import com.faforever.server.game.GameStateReport;
import com.faforever.server.game.HostGameRequest;
import com.faforever.server.game.JoinGameRequest;
import com.faforever.server.game.MutuallyAgreedDrawRequest;
import com.faforever.server.game.PlayerOptionReport;
import com.faforever.server.integration.legacy.transformer.RestoreGameSessionRequest;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.stats.ArmyStatisticsReport;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static com.faforever.server.integration.MessageHeaders.USER_HEADER;


@MessageEndpoint
public class GameServiceActivators {

  private final GameService gameService;

  public GameServiceActivators(GameService gameService) {
    this.gameService = gameService;
  }

  @ServiceActivator(inputChannel = ChannelNames.HOST_GAME_REQUEST)
  public void hostGameRequest(HostGameRequest request, @Header(USER_HEADER) Authentication authentication) {
    gameService.createGame(request.getTitle(), request.getMod(), request.getMapName(), request.getPassword(),
      request.getVisibility(), request.getMinRating(), request.getMaxRating(), getPlayer(authentication));
  }

  @ServiceActivator(inputChannel = ChannelNames.JOIN_GAME_REQUEST)
  public void joinGameRequest(JoinGameRequest request, @Header(USER_HEADER) Authentication authentication) {
    gameService.joinGame(request.getId(), request.getPassword(), getPlayer(authentication));
  }

  @ServiceActivator(inputChannel = ChannelNames.UPDATE_GAME_STATE_REQUEST)
  public void updateGameState(GameStateReport report, @Header(USER_HEADER) Authentication authentication) {
    gameService.updatePlayerGameState(report.getState(), getPlayer(authentication));
  }

  @ServiceActivator(inputChannel = ChannelNames.GAME_OPTION_REQUEST)
  public void updateGameOption(GameOptionReport report, @Header(USER_HEADER) Authentication authentication) {
    gameService.updateGameOption(getPlayer(authentication), report.getKey(), report.getValue());
  }

  @ServiceActivator(inputChannel = ChannelNames.PLAYER_OPTION_REQUEST)
  public void updatePlayerOption(PlayerOptionReport report, @Header(USER_HEADER) Authentication authentication) {
    gameService.updatePlayerOption(getPlayer(authentication), report.getPlayerId(), report.getKey(), report.getValue());
  }

  @ServiceActivator(inputChannel = ChannelNames.CLEAR_SLOT_REQUEST)
  public void clearSlot(ClearSlotRequest request, @Header(USER_HEADER) Authentication authentication) {
    gameService.clearSlot(getPlayer(authentication).getCurrentGame(), request.getSlotId());
  }

  @ServiceActivator(inputChannel = ChannelNames.AI_OPTION_REQUEST)
  public void updateAiOption(AiOptionReport report, @Header(USER_HEADER) Authentication authentication) {
    gameService.updateAiOption(getPlayer(authentication), report.getAiName(), report.getKey(), report.getValue());
  }

  @ServiceActivator(inputChannel = ChannelNames.DESYNC_REPORT)
  public void reportDesync(DesyncReport desyncReport, @Header(USER_HEADER) Authentication authentication) {
    gameService.reportDesync(getPlayer(authentication));
  }

  @ServiceActivator(inputChannel = ChannelNames.GAME_MODS_REPORT)
  public void updateGameMods(GameModsReport report, @Header(USER_HEADER) Authentication authentication) {
    gameService.updateGameMods(getPlayer(authentication).getCurrentGame(), report.getModUids());
  }

  @ServiceActivator(inputChannel = ChannelNames.GAME_MODS_COUNT_REPORT)
  public void updateGameModsCount(GameModsCountReport report, @Header(USER_HEADER) Authentication authentication) {
    gameService.updateGameModsCount(getPlayer(authentication).getCurrentGame(), report.getCount());
  }

  @ServiceActivator(inputChannel = ChannelNames.ARMY_OUTCOME_REPORT)
  public void reportArmyOutcome(ArmyOutcomeReport report, @Header(USER_HEADER) Authentication authentication) {
    gameService.reportArmyOutcome(getPlayer(authentication), report.getArmyId(), report.getOutcome());
  }

  @ServiceActivator(inputChannel = ChannelNames.ARMY_SCORE_REPORT)
  public void reportArmyScore(ArmyScoreReport report, @Header(USER_HEADER) Authentication authentication) {
    gameService.reportArmyScore(getPlayer(authentication), report.getArmyId(), report.getScore());
  }

  @ServiceActivator(inputChannel = ChannelNames.GAME_STATISTICS_REPORT)
  public void reportGameStatistics(ArmyStatisticsReport report, @Header(USER_HEADER) Authentication authentication) {
    gameService.reportArmyStatistics(getPlayer(authentication), report.getArmyStatistics());
  }

  @ServiceActivator(inputChannel = ChannelNames.ENFORCE_RATING_REQUEST)
  public void enforceRating(@Header(USER_HEADER) Authentication authentication) {
    gameService.enforceRating(getPlayer(authentication));
  }

  @ServiceActivator(inputChannel = ChannelNames.DISCONNECT_PEER_REQUEST)
  public void disconnectFromGame(DisconnectPeerRequest request, @Header(USER_HEADER) Authentication authentication) {
    gameService.disconnectPlayerFromGame(getPlayer(authentication), request.getPlayerId());
  }

  @ServiceActivator(inputChannel = ChannelNames.RESTORE_GAME_SESSION_REQUEST)
  public void restoreGameSession(RestoreGameSessionRequest request, @Header(USER_HEADER) Authentication authentication) {
    gameService.restoreGameSession(getPlayer(authentication), request.getGameId());
  }

  @ServiceActivator(inputChannel = ChannelNames.MUTUALLY_AGREED_DRAW_REQUEST)
  public void mutuallyAgreeDraw(MutuallyAgreedDrawRequest request, @Header(USER_HEADER) Authentication authentication) {
    gameService.mutuallyAgreeDraw(getPlayer(authentication));
  }

  @ServiceActivator(inputChannel = ChannelNames.CLIENT_DISCONNECTED_EVENT)
  public void onClientDisconnected(ClientDisconnectedEvent event) {
    Optional.ofNullable(event.getClientConnection().getAuthentication())
      .ifPresent(authentication -> gameService.removePlayer(((FafUserDetails) authentication.getPrincipal()).getPlayer()));
  }

  private Player getPlayer(Authentication authentication) {
    return ((FafUserDetails) authentication.getPrincipal()).getPlayer();
  }
}
