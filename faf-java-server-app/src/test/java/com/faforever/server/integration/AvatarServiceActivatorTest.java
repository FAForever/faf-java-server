package com.faforever.server.integration;

import com.faforever.server.avatar.AvatarService;
import com.faforever.server.avatar.ListAvatarsRequest;
import com.faforever.server.avatar.SelectAvatarRequest;
import com.faforever.server.player.Player;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AvatarServiceActivatorTest {

  private AvatarServiceActivator instance;

  @Mock
  private AvatarService avatarService;
  private TestingAuthenticationToken authentication;
  private Player player;

  @Before
  public void setUp() throws Exception {
    player = new Player();

    authentication = new TestingAuthenticationToken(new FafUserDetails((User) new User()
      .setPlayer(player).setPassword("pw").setLogin("JUnit")), null);

    instance = new AvatarServiceActivator(avatarService);
  }

  @Test
  public void selectAvatarByUrl() throws Exception {
    SelectAvatarRequest request = new SelectAvatarRequest(null, "http://example.com/foo.bar");

    instance.selectAvatar(request, authentication);

    verify(avatarService).selectAvatar(player, "http://example.com/foo.bar", request.getAvatarId());
  }

  @Test
  public void selectAvatarById() throws Exception {
    SelectAvatarRequest request = new SelectAvatarRequest(1, null);

    instance.selectAvatar(request, authentication);

    verify(avatarService).selectAvatar(player, null, 1);
  }

  @Test
  public void listAvatars() throws Exception {
    instance.listAvatars(ListAvatarsRequest.INSTANCE, authentication);

    verify(avatarService).sendAvatarList(player);
  }
}
