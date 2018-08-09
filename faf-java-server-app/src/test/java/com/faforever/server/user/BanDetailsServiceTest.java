package com.faforever.server.user;

import com.faforever.server.entity.BanDetails;
import com.faforever.server.entity.BanDetails.BanScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BanDetailsServiceTest {

  private BanDetailsService instance;

  @Mock
  private BanDetailsRepository banDetailsRepository;

  @Before
  public void setUp() throws Exception {
    instance = new BanDetailsService(banDetailsRepository);
  }

  @Test
  public void banUserGlobal() {
    instance.banUser(1, 2, BanScope.GLOBAL, "JUnit");

    ArgumentCaptor<BanDetails> captor = ArgumentCaptor.forClass(BanDetails.class);
    verify(banDetailsRepository).save(captor.capture());

    BanDetails banDetails = captor.getValue();
    assertThat(banDetails.getUser().getId(), is(1));
    assertThat(banDetails.getAuthor().getId(), is(2));
    assertThat(banDetails.getReason(), is("JUnit"));
    assertThat(banDetails.getExpiresAt(), is(nullValue()));
    assertThat(banDetails.getScope(), is(BanScope.GLOBAL));
  }

  @Test
  public void banUserChat() {
    instance.banUser(1, 2, BanScope.CHAT, "JUnit");

    ArgumentCaptor<BanDetails> captor = ArgumentCaptor.forClass(BanDetails.class);
    verify(banDetailsRepository).save(captor.capture());

    BanDetails banDetails = captor.getValue();
    assertThat(banDetails.getUser().getId(), is(1));
    assertThat(banDetails.getAuthor().getId(), is(2));
    assertThat(banDetails.getReason(), is("JUnit"));
    assertThat(banDetails.getExpiresAt(), is(nullValue()));
    assertThat(banDetails.getScope(), is(BanScope.CHAT));
  }
}
