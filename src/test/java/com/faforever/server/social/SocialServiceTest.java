package com.faforever.server.social;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.SocialRelation;
import com.faforever.server.entity.SocialRelationStatus;
import com.faforever.server.entity.User;
import com.faforever.server.player.PlayerOnlineEvent;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.social.SocialRelationListResponse.SocialRelation.RelationType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SocialServiceTest {

  private SocialService instance;

  @Mock
  private SocialRelationRepository socialRelationRepository;
  @Mock
  private ClientService clientService;
  private Player requester;

  @Captor
  private ArgumentCaptor<SocialRelation> captor;

  @Before
  public void setUp() throws Exception {
    requester = (Player) new Player().setId(1);

    instance = new SocialService(socialRelationRepository, clientService);
  }

  @Test
  public void addFriend() throws Exception {
    instance.addFriend(requester, 5);

    verify(socialRelationRepository).save(captor.capture());
    SocialRelation socialRelation = captor.getValue();

    assertThat(socialRelation.getStatus(), is(SocialRelationStatus.FRIEND));
    assertThat(socialRelation.getSubjectId(), is(5));
    assertThat(socialRelation.getPlayerId(), is(requester.getId()));
  }

  @Test
  public void addFoe() throws Exception {
    instance.addFoe(requester, 5);
    verify(socialRelationRepository).save(captor.capture());
    SocialRelation socialRelation = captor.getValue();

    assertThat(socialRelation.getStatus(), is(SocialRelationStatus.FOE));
    assertThat(socialRelation.getSubjectId(), is(5));
    assertThat(socialRelation.getPlayerId(), is(requester.getId()));
  }

  @Test
  public void removeFriend() throws Exception {
    instance.removeFriend(requester, 5);
    verify(socialRelationRepository).deleteByPlayerIdAndSubjectIdAndStatus(requester.getId(), 5, SocialRelationStatus.FRIEND);
  }

  @Test
  public void removeFoe() throws Exception {
    instance.removeFoe(requester, 5);
    verify(socialRelationRepository).deleteByPlayerIdAndSubjectIdAndStatus(requester.getId(), 5, SocialRelationStatus.FOE);
  }

  @Test
  public void onAuthenticationSuccess() throws Exception {
    User user = (User) new User()
      .setPlayer(new Player()
        .setSocialRelations(Arrays.asList(
          new SocialRelation(1, null, 10, SocialRelationStatus.FRIEND),
          new SocialRelation(2, null, 11, SocialRelationStatus.FOE)
        )))
      .setPassword("pw")
      .setLogin("junit");
    FafUserDetails userDetails = new FafUserDetails(user);

    instance.onPlayerOnlineEvent(new PlayerOnlineEvent(this, userDetails.getPlayer()));

    ArgumentCaptor<SocialRelationListResponse> captor = ArgumentCaptor.forClass(SocialRelationListResponse.class);
    verify(clientService).sendSocialRelations(captor.capture(), any());

    SocialRelationListResponse response = captor.getValue();
    assertThat(response, instanceOf(SocialRelationListResponse.class));

    assertThat(response.getSocialRelations(), hasSize(2));
    assertThat(response.getSocialRelations().get(0), is(new SocialRelationListResponse.SocialRelation(10, RelationType.FRIEND)));
    assertThat(response.getSocialRelations().get(1), is(new SocialRelationListResponse.SocialRelation(11, RelationType.FOE)));
  }

  @Test
  public void onAuthenticationSuccessNullRelations() throws Exception {
    User user = (User) new User()
      .setPlayer(new Player()
        .setSocialRelations(null))
      .setPassword("pw")
      .setLogin("junit");
    FafUserDetails userDetails = new FafUserDetails(user);

    instance.onPlayerOnlineEvent(new PlayerOnlineEvent(this, userDetails.getPlayer()));

    verify(clientService, never()).sendSocialRelations(any(), any());
  }
}
