package com.faforever.server.client;

import com.faforever.server.entity.Game;
import com.faforever.server.entity.Player;
import com.faforever.server.integration.ClientGateway;
import com.faforever.server.integration.Protocol;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ClientServiceTest {

  private ClientService instance;

  @Mock
  private ClientGateway clientGateway;

  private ClientConnection clientConnection;

  @Before
  public void setUp() throws Exception {
    clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16);

    instance = new ClientService(clientGateway);
  }

  @Test
  public void connectToPlayer() throws Exception {
    Player player = new Player();
    player.setClientConnection(clientConnection);

    Player peer = new Player();
    peer.setId(2);
    peer.setLogin("test");

    instance.connectToPlayer(player, peer);

    ArgumentCaptor<ConnectToPlayerResponse> captor = ArgumentCaptor.forClass(ConnectToPlayerResponse.class);
    verify(clientGateway).send(captor.capture(), eq(clientConnection));
    ConnectToPlayerResponse response = captor.getValue();

    assertThat(response.getPlayerId(), is(peer.getId()));
    assertThat(response.getPlayerName(), is(peer.getLogin()));
  }

  @Test
  public void connectToHost() throws Exception {
    Player player = new Player();
    player.setClientConnection(clientConnection);

    Player host = new Player();
    host.setId(1);

    Game game = new Game();
    game.setHost(host);

    instance.connectToHost(game, player);

    ArgumentCaptor<ConnectToHostResponse> captor = ArgumentCaptor.forClass(ConnectToHostResponse.class);
    verify(clientGateway).send(captor.capture(), eq(clientConnection));
    ConnectToHostResponse response = captor.getValue();

    assertThat(response.getHostId(), is(host.getId()));
  }
}
