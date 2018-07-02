package com.faforever.server.player;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.geoip.GeoIpService;
import com.faforever.server.integration.Protocol;
import com.faforever.server.security.FafUserDetails;
import io.micrometer.core.instrument.MeterRegistry;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.net.InetAddress;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PlayerServiceTest {
  private PlayerService instance;
  private Player player;

  @Mock
  private ClientService clientService;
  @Mock
  private MeterRegistry meterRegistry;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private GeoIpService geoIpService;

  @Before
  public void setUp() throws Exception {
    player = (Player) new Player().setId(1);
    player.setLogin("JUnit");
    instance = new PlayerService(clientService, meterRegistry, eventPublisher, geoIpService);
  }

  @Test
  public void onClientDisconnectRemovesPlayerAndUnsetsGameAndRemovesGameIfLastPlayer() {
    FafUserDetails fafUserDetails = createFafUserDetails();

    when(geoIpService.lookupTimezone(any())).thenReturn(Optional.of(TimeZone.getDefault()));
    when(geoIpService.lookupCountryCode(any())).thenReturn(Optional.of("CH"));

    instance.setPlayerOnline(fafUserDetails.getPlayer());
    assertThat(instance.getOnlinePlayer(player.getId()).isPresent(), is(true));
    assertThat(player.getTimeZone(), is(TimeZone.getDefault()));
    assertThat(player.getCountry(), is("CH"));

    instance.removePlayer(fafUserDetails.getPlayer());

    assertThat(instance.getOnlinePlayer(player.getId()).isPresent(), is(false));
  }

  @Test
  public void isPlayerOnline() {
    FafUserDetails fafUserDetails = createFafUserDetails();

    assertThat(instance.isPlayerOnline(fafUserDetails.getPlayer().getLogin()), is(false));
    instance.setPlayerOnline(fafUserDetails.getPlayer());
    assertThat(instance.isPlayerOnline(fafUserDetails.getPlayer().getLogin()), is(true));
  }

  private FafUserDetails createFafUserDetails() {
    User user = new User();
    user.setPassword("pw");
    user.setLogin(player.getLogin());
    user.setCountry("CH");
    user.setPlayer(player);

    player.setClientConnection(new ClientConnection("1", Protocol.V1_LEGACY_UTF_16, mock(InetAddress.class)));

    return new FafUserDetails(user);
  }

  @Test
  public void testPlayerOnlineUpdatesLastActive() {
    player.setLastActive(OffsetDateTime.now().minus(Duration.ofHours(1)));

    instance.setPlayerOnline(player);

    assertThat(player.getLastActive(), is(greaterThanOrEqualTo(OffsetDateTime.now().minus(Duration.ofMinutes(10)))));
  }
}
