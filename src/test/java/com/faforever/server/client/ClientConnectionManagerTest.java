package com.faforever.server.client;

import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.integration.Protocol;
import com.faforever.server.player.PlayerService;
import com.faforever.server.stats.Metrics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ApplicationEventPublisher;

import java.net.InetAddress;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientConnectionManagerTest {

  private ClientConnectionManager instance;

  @Mock
  private GaugeService gaugeService;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private PlayerService playerService;

  @Before
  public void setUp() throws Exception {
    instance = new ClientConnectionManager(gaugeService, playerService, eventPublisher);
  }

  @Test
  public void updateConnections() throws Exception {
    InetAddress inetAddress = mock(InetAddress.class);
    instance.obtainConnection("1", Protocol.LEGACY_UTF_16, inetAddress);
    verify(gaugeService).submit(Metrics.ACTIVE_CONNECTIONS, 1d);
    assertThat(instance.getConnections(), hasSize(1));

    instance.obtainConnection("1", Protocol.LEGACY_UTF_16, inetAddress);
    verify(gaugeService, times(2)).submit(Metrics.ACTIVE_CONNECTIONS, 1d);
    assertThat(instance.getConnections(), hasSize(1));

    instance.obtainConnection("2", Protocol.LEGACY_UTF_16, inetAddress);
    verify(gaugeService).submit(Metrics.ACTIVE_CONNECTIONS, 2d);
    assertThat(instance.getConnections(), hasSize(2));

    instance.removeConnection("1", Protocol.LEGACY_UTF_16);
    verify(gaugeService, times(3)).submit(Metrics.ACTIVE_CONNECTIONS, 1d);
    assertThat(instance.getConnections(), hasSize(1));

    instance.removeConnection("1", Protocol.LEGACY_UTF_16);
    verify(gaugeService, times(4)).submit(Metrics.ACTIVE_CONNECTIONS, 1d);
    assertThat(instance.getConnections(), hasSize(1));

    instance.removeConnection("2", Protocol.LEGACY_UTF_16);
    verify(gaugeService).submit(Metrics.ACTIVE_CONNECTIONS, 0d);
    assertThat(instance.getConnections(), hasSize(0));
  }

  @Test
  public void disconnectClient() throws Exception {
    ClientConnection clientConnection12 = new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class));
    Player player12 = new Player()
      .setClientConnection(clientConnection12);
    when(playerService.getPlayer(12)).thenReturn(Optional.of(player12));

    instance.disconnectClient(new User(), 12);

    ArgumentCaptor<CloseConnectionEvent> captor = ArgumentCaptor.forClass(CloseConnectionEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    CloseConnectionEvent value = captor.getValue();

    assertThat(value.getClientConnection(), is(clientConnection12));
  }
}
