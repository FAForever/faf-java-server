package com.faforever.server.avatar;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.RequestException;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AvatarServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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
  public void selectAvatarByUrl() throws Exception {
    instance.selectAvatar(player, "http://example.com/foo.bar", null);

    verify(avatarAssociationRepository).selectAvatar(1, "http://example.com/foo.bar");
  }

  @Test
  public void selectAvatarById() throws Exception {
    instance.selectAvatar(player, null, 1);

    verify(avatarAssociationRepository).selectAvatar(1, 1);
  }

  @Test
  public void selectAvatarNoIdNorUrl() throws Exception {
    expectedException.expect(RequestException.class);
    expectedException.expect(HasPropertyWithValue.hasProperty("errorCode", is(ErrorCode.EITHER_AVATAR_ID_OR_URL)));

    instance.selectAvatar(player, null, null);
  }
}
