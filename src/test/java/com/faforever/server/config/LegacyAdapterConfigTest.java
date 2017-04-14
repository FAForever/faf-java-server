package com.faforever.server.config;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientConnectionManager;
import com.faforever.server.client.CloseConnectionEvent;
import com.faforever.server.integration.Protocol;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
  private ServerProperties serverProperties;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private ClientConnectionManager clientConnectionManager;

  @Before
  public void setUp() throws Exception {
    instance = new LegacyAdapterConfig(serverProperties, applicationEventPublisher, clientConnectionManager);
  }

  @Test
  public void onConnectionClosed() throws Exception {
    TcpConnection connection = mock(TcpConnection.class);
    when(connection.getConnectionId()).thenReturn("1");
    when(serverProperties.getPort()).thenReturn(0);

    instance.onConnectionClosed(new TcpConnectionCloseEvent(connection, null));

    verify(clientConnectionManager).removeConnection("1", Protocol.LEGACY_UTF_16);
  }

  @Test
  public void onCloseConnection() throws Exception {
    ClientConnection clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class));
    instance.onCloseConnection(new CloseConnectionEvent(this, clientConnection));
    // Not much to assert here
  }
}
