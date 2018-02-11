package com.faforever.server.social;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.integration.Protocol;
import com.faforever.server.security.FafUserDetails;
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
public class SocialServiceActivatorsTest {
  private SocialServiceActivators instance;

  @Mock
  private SocialService socialService;

  private ClientConnection clientConnection;
  private Player player;

  @Before
  public void setUp() throws Exception {
    player = (Player) new Player().setId(1);

    clientConnection = new ClientConnection("1", Protocol.V1_LEGACY_UTF_16, mock(InetAddress.class));
    clientConnection.setAuthentication(new TestingAuthenticationToken(new FafUserDetails((User) new User()
      .setPlayer(player).setPassword("pw").setLogin("JUnit")), null));

    instance = new SocialServiceActivators(socialService);
  }

  @Test
  public void addFriend() throws Exception {
    instance.addFriend(new AddFriendRequest(10), clientConnection.getAuthentication());
    verify(socialService).addFriend(player, 10);
  }

  @Test
  public void addFoe() throws Exception {
    instance.addFoe(new AddFoeRequest(10), clientConnection.getAuthentication());
    verify(socialService).addFoe(player, 10);
  }

  @Test
  public void removeFriend() throws Exception {
    instance.removeFriend(new RemoveFriendRequest(10), clientConnection.getAuthentication());
    verify(socialService).removeFriend(player, 10);
  }

  @Test
  public void removeFoe() throws Exception {
    instance.removeFoe(new RemoveFoeRequest(10), clientConnection.getAuthentication());
    verify(socialService).removeFoe(player, 10);
  }
}
