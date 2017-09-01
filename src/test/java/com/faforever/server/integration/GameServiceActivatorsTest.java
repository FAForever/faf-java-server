package com.faforever.server.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.game.DisconnectPeerRequest;
import com.faforever.server.game.GameService;
import com.faforever.server.integration.legacy.transformer.RestoreGameSessionRequest;
import com.faforever.server.security.FafUserDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.net.InetAddress;

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
    clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class));
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
}
