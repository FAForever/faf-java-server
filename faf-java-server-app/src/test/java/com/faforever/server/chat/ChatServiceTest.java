package com.faforever.server.chat;

import com.faforever.server.clan.Clan;
import com.faforever.server.clan.ClanMembership;
import com.faforever.server.client.ClientService;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.config.ServerProperties.Chat;
import com.faforever.server.player.Player;
import com.faforever.server.player.PlayerOnlineEvent;
import com.faforever.server.security.GroupAssociation;
import com.faforever.server.security.GroupAssociation.Group;
import com.faforever.server.security.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChatServiceTest {
  private ChatService instance;

  @Mock
  private ClientService clientService;

  @Before
  public void setUp() throws Exception {
    ServerProperties properties = new ServerProperties();

    Chat chat = properties.getChat();
    chat.setAdminChannels(Collections.singletonList("#admins"));
    chat.setModeratorChannels(Collections.singletonList("#moderators"));
    chat.setDefaultChannels(Arrays.asList("#foo", "#bar"));
    chat.setClanChannelFormat("#clan_%s");

    instance = new ChatService(properties, clientService);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void onAuthenticationSuccessJoinsChannels() throws Exception {
    testJoinChannels(null, "#foo", "#bar", "#clan_junit");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void onAuthenticationSuccessJoinsAdminChannels() throws Exception {
    testJoinChannels(Group.ADMIN, "#admins", "#foo", "#bar", "#clan_junit");
  }

  @Test
  public void onAuthenticationSuccessJoinsModeratorChannels() throws Exception {
    testJoinChannels(Group.MODERATOR, "#moderators", "#foo", "#bar", "#clan_junit");
  }

  @SuppressWarnings("unchecked")
  private void testJoinChannels(Group group, String... expectedChannels) {
    User user = (User) new User()
      .setGroupAssociation(group == null ? null : new GroupAssociation().setGroup(group))
      .setPassword("pw")
      .setLogin("junit");

    Player player = new Player()
      .setUser(user)
      .setClanMemberships(Collections.singletonList(new ClanMembership().setClan(new Clan().setTag("junit"))));

    instance.onPlayerOnlineEvent(new PlayerOnlineEvent(this, player));

    ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass((Class) Set.class);
    verify(clientService).sendChatChannels(captor.capture(), any());

    Set<String> channels = captor.getValue();
    assertThat(channels, containsInAnyOrder(expectedChannels));
  }
}
