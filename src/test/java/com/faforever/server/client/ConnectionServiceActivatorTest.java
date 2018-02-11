package com.faforever.server.client;

import com.faforever.server.entity.User;
import com.faforever.server.integration.Protocol;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.net.InetAddress;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceActivatorTest {

  private ConnectionServiceActivator instance;

  @Mock
  private ClientConnectionService clientConnectionService;

  private ClientConnection clientConnection;
  private User user;

  @Before
  public void setUp() throws Exception {
    user = (User) new User()
      .setPassword("password")
      .setLogin("JUnit");

    clientConnection = new ClientConnection("1", Protocol.V1_LEGACY_UTF_16, mock(InetAddress.class))
      .setAuthentication(new TestingAuthenticationToken(user, null));

    instance = new ConnectionServiceActivator(clientConnectionService);
  }

  @Test
  public void disconnectClientRequest() throws Exception {
    DisconnectClientRequest request = new DisconnectClientRequest(1);

    instance.disconnectClientRequest(request, clientConnection);

    verify(clientConnectionService).disconnectClient(new TestingAuthenticationToken(user, null), 1);
  }
}
