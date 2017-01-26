package com.faforever.server.player;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.geoip.GeoIpService;
import com.faforever.server.integration.Protocol;
import com.faforever.server.security.FafUserDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

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
  @Mock
  private GeoIpService geoIpService;

  @Before
  public void setUp() throws Exception {
    player = (Player) new Player().setId(1);
    instance = new PlayerService(clientService);
  }

  @Test
  public void onClientDisconnectRemovesPlayerAndUnsetsGameAndRemovesGameIfLastPlayer() throws Exception {
    User user = new User();
    user.setPassword("pw");
    user.setLogin("JUnit");
    user.setCountry("CH");
    user.setPlayer(player);

    player.setClientConnection(new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class)));

    FafUserDetails fafUserDetails = new FafUserDetails(user);
    instance.onAuthenticationSuccess(new AuthenticationSuccessEvent(new TestingAuthenticationToken(fafUserDetails, "pw")));
    assertThat(instance.getPlayer(player.getId()).isPresent(), is(true));

    InetAddress inetAddress = mock(InetAddress.class);
    ClientConnection clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16, inetAddress)
      .setUserDetails(new FafUserDetails(user));

    instance.onClientDisconnect(new ClientDisconnectedEvent(this, clientConnection));

    assertThat(instance.getPlayer(player.getId()).isPresent(), is(false));
  }
}
