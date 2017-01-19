package com.faforever.server.player;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.integration.Protocol;
import com.faforever.server.security.FafUserDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PlayerServiceTest {
  private PlayerService instance;
  private Player player;

  @Before
  public void setUp() throws Exception {
    player = (Player) new Player()
      .setId(1);

    instance = new PlayerService();
  }

  @Test
  public void onClientDisconnectRemovesPlayerAndUnsetsGameAndRemovesGameIfLastPlayer() throws Exception {
    instance.onPlayerLogin(new PlayerLoginEvent(player));
    assertThat(instance.getPlayer(player.getId()).isPresent(), is(true));

    User user = new User();
    user.setPassword("pw");
    user.setLogin("JUnit");
    user.setPlayer(player);

    ClientConnection clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16)
      .setUserDetails(new FafUserDetails(user));

    instance.onClientDisconnect(new ClientDisconnectedEvent(this, clientConnection));

    assertThat(instance.getPlayer(player.getId()).isPresent(), is(false));
  }
}
