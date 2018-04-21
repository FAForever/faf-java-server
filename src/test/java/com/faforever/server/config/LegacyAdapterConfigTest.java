package com.faforever.server.config;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientConnectionService;
import com.faforever.server.client.CloseConnectionEvent;
import com.faforever.server.config.integration.LegacyAdapterConfig;
import com.faforever.server.integration.Protocol;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.integration.ip.tcp.connection.TcpConnectionCloseEvent;

import java.net.InetAddress;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LegacyAdapterConfigTest {
  private LegacyAdapterConfig instance;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private ClientConnectionService clientConnectionService;
  private ServerProperties serverProperties;

  @Before
  public void setUp() throws Exception {
    serverProperties = new ServerProperties();

    instance = new LegacyAdapterConfig(serverProperties, applicationEventPublisher, clientConnectionService);
  }

  @Test
  public void onConnectionClosed() throws Exception {
    TcpConnection connection = mock(TcpConnection.class);
    when(connection.getConnectionId()).thenReturn("1");
    serverProperties.setPort(0);

    instance.onConnectionClosed(new TcpConnectionCloseEvent(connection, null));

    verify(clientConnectionService).removeConnection("1", Protocol.V1_LEGACY_UTF_16);
  }

  @Test
  public void onCloseConnection() throws Exception {
    ClientConnection clientConnection = new ClientConnection("1", Protocol.V1_LEGACY_UTF_16, mock(InetAddress.class));
    instance.onCloseConnection(new CloseConnectionEvent(this, clientConnection));
    // Not much to assert here
  }
}
