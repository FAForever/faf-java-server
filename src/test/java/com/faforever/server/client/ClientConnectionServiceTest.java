package com.faforever.server.client;

import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.integration.Protocol;
import com.faforever.server.player.PlayerService;
import com.faforever.server.stats.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.net.InetAddress;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientConnectionServiceTest {

  private ClientConnectionService instance;

  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private PlayerService playerService;
  private SimpleMeterRegistry meterRegistry;

  @Before
  public void setUp() throws Exception {
    meterRegistry = new SimpleMeterRegistry();
    instance = new ClientConnectionService(meterRegistry, playerService, eventPublisher);
  }

  @Test
  public void updateConnections() {
    InetAddress inetAddress = mock(InetAddress.class);
    instance.createClientConnection("1", Protocol.V1_LEGACY_UTF_16, inetAddress);

    assertThat(meterRegistry.find(Metrics.CLIENTS).tag(ClientConnectionService.TAG_PROTOCOL, Protocol.V1_LEGACY_UTF_16.name()).gauge().value(), is(1d));
    assertThat(instance.getConnections(), hasSize(1));

    instance.createClientConnection("2", Protocol.V1_LEGACY_UTF_16, inetAddress);
    assertThat(meterRegistry.find(Metrics.CLIENTS).tag(ClientConnectionService.TAG_PROTOCOL, Protocol.V1_LEGACY_UTF_16.name()).gauge().value(), is(2d));
    assertThat(instance.getConnections(), hasSize(2));

    instance.removeConnection("1", Protocol.V1_LEGACY_UTF_16);
    assertThat(meterRegistry.find(Metrics.CLIENTS).tag(ClientConnectionService.TAG_PROTOCOL, Protocol.V1_LEGACY_UTF_16.name()).gauge().value(), is(1d));
    assertThat(instance.getConnections(), hasSize(1));

    // This connection has already been removed, so expect no change
    instance.removeConnection("1", Protocol.V1_LEGACY_UTF_16);
    assertThat(meterRegistry.find(Metrics.CLIENTS).tag(ClientConnectionService.TAG_PROTOCOL, Protocol.V1_LEGACY_UTF_16.name()).gauge().value(), is(1d));
    assertThat(instance.getConnections(), hasSize(1));

    instance.removeConnection("2", Protocol.V1_LEGACY_UTF_16);
    assertThat(meterRegistry.find(Metrics.CLIENTS).tag(ClientConnectionService.TAG_PROTOCOL, Protocol.V1_LEGACY_UTF_16.name()).gauge().value(), is(0d));
    assertThat(instance.getConnections(), hasSize(0));
  }

  @Test
  public void disconnectClient() {
    ClientConnection clientConnection12 = new ClientConnection("1", Protocol.V1_LEGACY_UTF_16, mock(InetAddress.class));
    Player player12 = new Player()
      .setClientConnection(clientConnection12);
    when(playerService.getOnlinePlayer(12)).thenReturn(Optional.of(player12));

    instance.disconnectClient(new TestingAuthenticationToken(new User(), null), 12);

    ArgumentCaptor<CloseConnectionEvent> captor = ArgumentCaptor.forClass(CloseConnectionEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    CloseConnectionEvent value = captor.getValue();

    assertThat(value.getClientConnection(), is(clientConnection12));
  }
}
