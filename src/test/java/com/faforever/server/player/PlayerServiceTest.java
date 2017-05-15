package com.faforever.server.player;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.integration.Protocol;
import com.faforever.server.security.FafUserDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class PlayerServiceTest {
  private PlayerService instance;
  private Player player;

  @Mock
  private ClientService clientService;

  @Before
  public void setUp() throws Exception {
    player = (Player) new Player().setId(1);
    player.setLogin("JUnit");
    instance = new PlayerService(clientService);
  }

  @Test
  public void onClientDisconnectRemovesPlayerAndUnsetsGameAndRemovesGameIfLastPlayer() throws Exception {
    FafUserDetails fafUserDetails = createFafUserDetails();

    instance.onPlayerOnlineEvent(new PlayerOnlineEvent(this, fafUserDetails.getPlayer()));
    assertThat(instance.getOnlinePlayer(player.getId()).isPresent(), is(true));

    instance.removePlayer(fafUserDetails.getPlayer());

    assertThat(instance.getOnlinePlayer(player.getId()).isPresent(), is(false));
  }

  @Test
  public void isPlayerOnline() {
    FafUserDetails fafUserDetails = createFafUserDetails();

    assertThat(instance.isPlayerOnline(fafUserDetails.getUser().getPlayer().getLogin()), is(false));
    instance.onPlayerOnlineEvent(new PlayerOnlineEvent(this, fafUserDetails.getPlayer()));
    assertThat(instance.isPlayerOnline(fafUserDetails.getUser().getPlayer().getLogin()), is(true));
  }

  private FafUserDetails createFafUserDetails() {
    User user = new User();
    user.setPassword("pw");
    user.setLogin(player.getLogin());
    user.setCountry("CH");
    user.setPlayer(player);

    player.setClientConnection(new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class)));

    return new FafUserDetails(user);
  }
}
