package com.faforever.server.client;

import com.faforever.server.integration.Protocol;
import com.faforever.server.stats.Metrics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ApplicationEventPublisher;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ClientConnectionManagerTest {

  private ClientConnectionManager instance;

  @Mock
  private GaugeService gaugeService;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Before
  public void setUp() throws Exception {
    instance = new ClientConnectionManager(gaugeService, applicationEventPublisher);
  }

  @Test
  public void updateConnections() throws Exception {
    instance.obtainConnection("1", Protocol.LEGACY_UTF_16);
    verify(gaugeService).submit(Metrics.ACTIVE_CONNECTIONS, 1d);
    assertThat(instance.getConnections(), hasSize(1));

    instance.obtainConnection("1", Protocol.LEGACY_UTF_16);
    verify(gaugeService, times(2)).submit(Metrics.ACTIVE_CONNECTIONS, 1d);
    assertThat(instance.getConnections(), hasSize(1));

    instance.obtainConnection("2", Protocol.LEGACY_UTF_16);
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
}
