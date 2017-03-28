package com.faforever.server.chat;

import com.faforever.server.client.ClientService;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.config.ServerProperties.Chat;
import com.faforever.server.entity.GroupAssociation;
import com.faforever.server.entity.GroupAssociation.Group;
import com.faforever.server.entity.User;
import com.faforever.server.security.FafUserDetails;
import com.google.common.hash.Hashing;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import java.nio.charset.StandardCharsets;
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
  private NickCoreRepository nickCoreRepository;
  @Mock
  private ClientService clientService;

  @Before
  public void setUp() throws Exception {
    ServerProperties properties = new ServerProperties();

    Chat chat = properties.getChat();
    chat.setAdminChannels(Collections.singletonList("#admins"));
    chat.setModeratorChannels(Collections.singletonList("#moderators"));
    chat.setDefaultChannels(Arrays.asList("#foo", "#bar"));

    instance = new ChatService(nickCoreRepository, properties, clientService);
  }

  @Test
  public void updateIrcPassword() throws Exception {
    instance.updateIrcPassword("junit", "1234");

    verify(nickCoreRepository).updatePassword("junit", Hashing.md5().hashString("1234", StandardCharsets.UTF_8).toString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void onAuthenticationSuccessJoinsChannels() throws Exception {
    testJoinChannels(null, "#foo", "#bar");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void onAuthenticationSuccessJoinsAdminChannels() throws Exception {
    testJoinChannels(Group.ADMIN, "#admins", "#foo", "#bar");
  }

  @Test
  public void onAuthenticationSuccessJoinsModeratorChannels() throws Exception {
    testJoinChannels(Group.MODERATOR, "#moderators", "#foo", "#bar");
  }

  @SuppressWarnings("unchecked")
  private void testJoinChannels(Group group, String... expectedChannels) {
    User user = (User) new User()
      .setPassword("pw")
      .setGroupAssociation(group == null ? null : new GroupAssociation().setGroup(group))
      .setLogin("junit");
    TestingAuthenticationToken authentication = new TestingAuthenticationToken(new FafUserDetails(user), "foo");

    instance.onAuthenticationSuccess(new AuthenticationSuccessEvent(authentication));

    ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass((Class) Set.class);
    verify(clientService).sendChatChannels(captor.capture(), any());

    Set<String> channels = captor.getValue();
    assertThat(channels, containsInAnyOrder(expectedChannels));
  }

  // TODO test clan channel as well
}
