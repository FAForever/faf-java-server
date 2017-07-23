package com.faforever.server.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
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
import com.faforever.server.game.GameVisibility;
import com.faforever.server.game.HostGameRequest;
import com.faforever.server.game.JoinGameRequest;
import com.faforever.server.game.MutuallyAgreedDrawRequest;
import com.faforever.server.game.Outcome;
import com.faforever.server.game.PlayerGameState;
import com.faforever.server.game.PlayerOptionReport;
import com.faforever.server.integration.legacy.transformer.RestoreGameSessionRequest;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.stats.ArmyStatisticsReport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GameServiceActivatorsTest {

  private GameServiceActivators instance;

  @Mock
  private GameService gameService;
  private ClientConnection clientConnection;
  private Player player;

  @Before
  public void setUp() throws Exception {
    clientConnection = new ClientConnection("1", Protocol.V1_LEGACY_UTF_16, mock(InetAddress.class));
    player = new Player();
    User user = (User) new User().setPlayer(player).setPassword("password").setLogin("JUnit");
    clientConnection.setAuthentication(new TestingAuthenticationToken(new FafUserDetails(user), null));

    instance = new GameServiceActivators(gameService);
  }

  @Test
  public void disconnectFromGame() throws Exception {
    instance.disconnectFromGame(new DisconnectPeerRequest(13), clientConnection.getAuthentication());
    verify(gameService).disconnectPlayerFromGame(player, 13);
  }

  @Test
  public void restoreGameSession() throws Exception {
    instance.restoreGameSession(new RestoreGameSessionRequest(5), clientConnection.getAuthentication());
    verify(gameService).restoreGameSession(player, 5);
  }

  @Test
  public void hostGameRequest() throws Exception {
    instance.hostGameRequest(new HostGameRequest("scmp01", "Title", "mod", "pw", GameVisibility.PUBLIC, 600, 900), clientConnection.getAuthentication());
    verify(gameService).createGame("Title", "mod", "scmp01", "pw", GameVisibility.PUBLIC, 600, 900, player);
  }

  @Test
  public void joinGameRequest() throws Exception {
    instance.joinGameRequest(new JoinGameRequest(1, "pw"), clientConnection.getAuthentication());
    verify(gameService).joinGame(1, "pw", player);
  }

  @Test
  public void updateGameState() throws Exception {
    instance.updateGameState(new GameStateReport(PlayerGameState.LAUNCHING), clientConnection.getAuthentication());
    verify(gameService).updatePlayerGameState(PlayerGameState.LAUNCHING, player);
  }

  @Test
  public void updateGameOption() throws Exception {
    instance.updateGameOption(new GameOptionReport("key", "value"), clientConnection.getAuthentication());
    verify(gameService).updateGameOption(player, "key", "value");
  }

  @Test
  public void updatePlayerOption() throws Exception {
    instance.updatePlayerOption(new PlayerOptionReport(1, "key", "value"), clientConnection.getAuthentication());
    verify(gameService).updatePlayerOption(player, 1, "key", "value");
  }

  @Test
  public void clearSlot() throws Exception {
    instance.clearSlot(ClearSlotRequest.valueOf(1), clientConnection.getAuthentication());
    verify(gameService).clearSlot(player.getCurrentGame(), 1);
  }

  @Test
  public void updateAiOption() throws Exception {
    instance.updateAiOption(new AiOptionReport("ai", "key", "value"), clientConnection.getAuthentication());
    verify(gameService).updateAiOption(player, "ai", "key", "value");
  }

  @Test
  public void reportDesync() throws Exception {
    instance.reportDesync(DesyncReport.INSTANCE, clientConnection.getAuthentication());
    verify(gameService).reportDesync(player);
  }

  @Test
  public void updateGameMods() throws Exception {
    instance.updateGameMods(new GameModsReport(Arrays.asList("A", "B")), clientConnection.getAuthentication());
    verify(gameService).updateGameMods(player.getCurrentGame(), Arrays.asList("A", "B"));
  }

  @Test
  public void updateGameModsCount() throws Exception {
    instance.updateGameModsCount(new GameModsCountReport(3), clientConnection.getAuthentication());
    verify(gameService).updateGameModsCount(player.getCurrentGame(), 3);
  }

  @Test
  public void reportArmyOutcome() throws Exception {
    instance.reportArmyOutcome(ArmyOutcomeReport.valueOf(1, Outcome.VICTORY), clientConnection.getAuthentication());
    verify(gameService).reportArmyOutcome(player, 1, Outcome.VICTORY);
  }

  @Test
  public void reportArmyScore() throws Exception {
    instance.reportArmyScore(new ArmyScoreReport(1, 10), clientConnection.getAuthentication());
    verify(gameService).reportArmyScore(player, 1, 10);
  }

  @Test
  public void reportGameStatistics() throws Exception {
    ArmyStatisticsReport report = new ArmyStatisticsReport(Collections.emptyList());
    instance.reportGameStatistics(report, clientConnection.getAuthentication());
    verify(gameService).reportArmyStatistics(player, report.getArmyStatistics());
  }

  @Test
  public void enforceRating() throws Exception {
    instance.enforceRating(clientConnection.getAuthentication());
    verify(gameService).enforceRating(player);
  }

  @Test
  public void mutuallyAgreeDraw() throws Exception {
    instance.mutuallyAgreeDraw(MutuallyAgreedDrawRequest.INSTANCE, clientConnection.getAuthentication());
    verify(gameService).mutuallyAgreeDraw(player);
  }

  @Test
  public void onClientDisconnected() throws Exception {
    instance.onClientDisconnected(new ClientDisconnectedEvent(this, clientConnection));
    verify(gameService).removePlayer(player);
  }
}
