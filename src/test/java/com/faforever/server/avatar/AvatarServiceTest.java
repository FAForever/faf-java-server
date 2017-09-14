package com.faforever.server.avatar;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AvatarServiceTest {
  private AvatarService instance;

  @Mock
  private AvatarAssociationRepository avatarAssociationRepository;
  @Mock
  private ClientService clientService;

  private Player player;

  @Before
  public void setUp() throws Exception {
    player = (Player) new Player().setId(1);

    instance = new AvatarService(avatarAssociationRepository, clientService);
  }

  @Test
  public void selectAvatar() throws Exception {
    instance.selectAvatar(player, "http://example.com/foo.bar");

    verify(avatarAssociationRepository).selectAvatar(1, "http://example.com/foo.bar");
  }
}
