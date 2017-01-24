package com.faforever.server.client;

import com.faforever.server.entity.User;
import com.faforever.server.integration.Protocol;
import com.faforever.server.security.FafUserDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceActivatorTest {

  private ConnectionServiceActivator instance;

  @Mock
  private ClientConnectionManager clientConnectionManager;

  private ClientConnection clientConnection;
  private User user;

  @Before
  public void setUp() throws Exception {
    user = (User) new User()
      .setPassword("password")
      .setLogin("JUnit");

    clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class))
      .setUserDetails(new FafUserDetails(user));

    instance = new ConnectionServiceActivator(clientConnectionManager);
  }

  @Test
  public void disconnectClientRequest() throws Exception {
    DisconnectClientRequest request = new DisconnectClientRequest(1);

    instance.disconnectClientRequest(request, clientConnection);

    verify(clientConnectionManager).disconnectClient(user, 1);
  }
}
