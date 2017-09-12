package com.faforever.server.integration;

import com.faforever.server.avatar.AvatarService;
import com.faforever.server.avatar.ListAvatarsMessage;
import com.faforever.server.avatar.SelectAvatarRequest;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.security.FafUserDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
  public void selectAvatar() throws Exception {
    SelectAvatarRequest request = new SelectAvatarRequest("http://example.com/foo.bar");

    instance.selectAvatar(request, authentication);

    verify(avatarService).selectAvatar(player, "http://example.com/foo.bar");
  }

  @Test
  public void listAvatars() throws Exception {
    instance.listAvatars(ListAvatarsMessage.INSTANCE, authentication);

    verify(avatarService).sendAvatarList(player);
  }
}
