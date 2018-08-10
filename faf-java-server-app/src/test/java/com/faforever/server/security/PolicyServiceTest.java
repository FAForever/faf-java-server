package com.faforever.server.security;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.RequestException;
import com.faforever.server.player.Player;
import com.faforever.server.security.BanDetails.BanScope;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PolicyServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private PolicyService instance;
  private ServerProperties properties;
  private Player player;

  @Mock
  private BanDetailsService banDetailsService;

  @Mock
  private RestTemplate restTemplate;

  @Before
  public void setUp() throws Exception {
    player = (Player) new Player().setId(51234);
    properties = new ServerProperties();
    properties.getPolicyService().setUrl("http://example.com");

    instance = new PolicyService(properties, banDetailsService, restTemplate);
  }

  @Test
  public void verifyVm() {
    try {
      callVerifyAndReturn("vm");
    } catch (RequestException e) {
      assertThat(e.getErrorCode(), is(ErrorCode.UID_VM));
    }

    verifyZeroInteractions(banDetailsService);
  }

  @Test
  public void verifyAlreadyAssociated() {
    try {
      callVerifyAndReturn("already_associated");
    } catch (RequestException e) {
      assertThat(e.getErrorCode(), is(ErrorCode.UID_ALREADY_ASSOCIATED));
    }

    verifyZeroInteractions(banDetailsService);
  }

  @Test
  public void verifyFraudulent() {
    try {
      callVerifyAndReturn("fraudulent");
      fail("No request exception has been thrown");
    } catch (RequestException e) {
      verify(banDetailsService).banUser(player.getId(), player.getId(), BanScope.GLOBAL, "Auto-banned because of fraudulent login attempt");
    }
  }

  @Test
  public void verifyHonest() {
    callVerifyAndReturn("honest");

    verifyZeroInteractions(banDetailsService);
  }

  @Test
  public void verifySkipsIfDisabledWithNull() {
    properties.getPolicyService().setUrl(null);
    instance = new PolicyService(properties, banDetailsService, restTemplate);

    instance.verify(player, "aaa", "123");

    verifyZeroInteractions(restTemplate);
  }

  @Test
  public void verifySkipsIfDisabledWithEmptyString() {
    properties.getPolicyService().setUrl("");
    instance = new PolicyService(properties, banDetailsService, restTemplate);

    instance.verify(player, "aaa", "123");

    verifyZeroInteractions(restTemplate);
  }

  @Test
  public void verifySkipsIfDisabledWithFalse() {
    properties.getPolicyService().setUrl("false");
    instance = new PolicyService(properties, banDetailsService, restTemplate);

    instance.verify(player, "aaa", "123");

    verifyZeroInteractions(restTemplate);
  }

  @Test
  public void verifySkipsIfExemptAvailable() {
    player.setUniqueIdExempt(new UniqueIdExempt());

    instance.verify(player, "aaa", "123");

    verifyZeroInteractions(restTemplate);
  }

  @Test
  public void verifySkipsIfSteamIdAvailable() {
    player.setSteamId("123");

    instance.verify(player, "aaa", "123");

    verifyZeroInteractions(restTemplate);
  }

  private void callVerifyAndReturn(String policyServerResponse) {
    when(restTemplate.postForObject("/verify", ImmutableMap.of(
      "player_id", player.getId(),
      "uid_hash", "aaa",
      "session", "123"
    ), String.class)).thenReturn(policyServerResponse);

    instance.verify(player, "aaa", "123");

    verify(restTemplate).postForObject("/verify", ImmutableMap.of(
      "player_id", player.getId(),
      "uid_hash", "aaa",
      "session", "123"
    ), String.class);
  }
}
