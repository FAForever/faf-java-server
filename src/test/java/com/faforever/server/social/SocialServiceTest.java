package com.faforever.server.social;

import com.faforever.server.entity.Player;
import com.faforever.server.entity.SocialRelation;
import com.faforever.server.entity.SocialRelationStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SocialServiceTest {

  private SocialService instance;

  @Mock
  private SocialRelationRepository socialRelationRepository;
  private Player requester;

  @Captor
  private ArgumentCaptor<SocialRelation> captor;

  @Before
  public void setUp() throws Exception {
    requester = (Player) new Player().setId(1);

    instance = new SocialService(socialRelationRepository);
  }

  @Test
  public void addFriend() throws Exception {
    instance.addFriend(requester, 5);

    verify(socialRelationRepository).save(captor.capture());
    SocialRelation socialRelation = captor.getValue();

    assertThat(socialRelation.getStatus(), is(SocialRelationStatus.FRIEND));
    assertThat(socialRelation.getSubjectId(), is(5));
    assertThat(socialRelation.getUserId(), is(requester.getId()));
  }

  @Test
  public void addFoe() throws Exception {
    instance.addFoe(requester, 5);
    verify(socialRelationRepository).save(captor.capture());
    SocialRelation socialRelation = captor.getValue();

    assertThat(socialRelation.getStatus(), is(SocialRelationStatus.FOE));
    assertThat(socialRelation.getSubjectId(), is(5));
    assertThat(socialRelation.getUserId(), is(requester.getId()));
  }

  @Test
  public void removeFriend() throws Exception {
    instance.removeFriend(requester, 5);
    verify(socialRelationRepository).deleteByUserIdAndSubjectIdAndStatus(requester.getId(), 5, SocialRelationStatus.FRIEND);
  }

  @Test
  public void removeFoe() throws Exception {
    instance.removeFoe(requester, 5);
    verify(socialRelationRepository).deleteByUserIdAndSubjectIdAndStatus(requester.getId(), 5, SocialRelationStatus.FOE);
  }
}
