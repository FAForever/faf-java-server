package com.faforever.server.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.ice.IceMessage;
import com.faforever.server.ice.IceServersRequest;
import com.faforever.server.ice.IceService;
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
public class IceServiceActivatorsTest {
  private IceServiceActivators instance;

  @Mock
  private IceService iceService;
  private Player player;
  private ClientConnection clientConnection;

  @Before
  public void setUp() throws Exception {
    player = new Player();
    player.setClientConnection(clientConnection);

    clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class))
      .setUserDetails(new FafUserDetails((User) new User().setPlayer(player).setPassword("pw").setLogin("JUnit")));

    instance = new IceServiceActivators(iceService);
  }

  @Test
  public void requestIceServers() throws Exception {
    instance.requestIceServers(new IceServersRequest(), clientConnection);

    verify(iceService).requestIceServers(player);
  }

  @Test
  public void forwardIceMessage() throws Exception {
    Object payload = new Object();
    instance.forwardIceMessage(new IceMessage(42, payload), clientConnection);

    verify(iceService).forwardIceMessage(player, 42, payload);
  }
}
