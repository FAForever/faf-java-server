package com.faforever.server.integration.legacy;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientService;
import com.faforever.server.client.LegacyLoginRequest;
import com.faforever.server.client.LegacySessionRequest;
import com.faforever.server.client.ListCoopRequest;
import com.faforever.server.client.SessionResponse;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.integration.Protocol;
import com.faforever.server.player.PlayerService;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.PolicyService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.net.InetAddress;

import static com.faforever.server.error.RequestExceptionWithCode.requestExceptionWithCode;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LegacyServicesActivatorsTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private LegacyServicesActivators instance;

  @Mock
  private AuthenticationManager authenticationManager;
  @Mock
  private ClientService clientService;
  @Mock
  private PolicyService policyService;
  @Mock
  private PlayerService playerService;

  private ClientConnection clientConnection;
  private Player player;

  @Before
  public void setUp() throws Exception {
    clientConnection = new ClientConnection("1", Protocol.V1_LEGACY_UTF_16, mock(InetAddress.class));
    player = new Player();
    player.setClientConnection(clientConnection);

    instance = new LegacyServicesActivators(authenticationManager, clientService, policyService,
      playerService);
  }

  @Test
  public void askSession() {
    assertThat(clientConnection.getUserAgent(), is(nullValue()));

    SessionResponse sessionResponse = instance.askSession(LegacySessionRequest.forUserAgent("junit"), clientConnection);

    assertThat(sessionResponse, is(SessionResponse.INSTANCE));
    assertThat(clientConnection.getUserAgent(), is("junit"));
  }

  @Test
  public void loginRequest() {
    createAuthentication(player);

    instance.loginRequest(new LegacyLoginRequest("JUnit", "password", "uniqueId"), clientConnection);

    ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
    verify(authenticationManager).authenticate(captor.capture());
    UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) captor.getValue();

    assertThat(authentication.getPrincipal(), is("JUnit"));
    assertThat(authentication.getCredentials(), is("password"));

    verify(playerService).setPlayerOnline(player);
  }

  @Test
  public void loginRequestCallsUniqueIdService() {
    createAuthentication(new Player());

    instance.loginRequest(new LegacyLoginRequest("JUnit", "password", "uniqueId"), clientConnection);

    verify(policyService).verify(any(), eq("uniqueId"), eq("1"));
  }

  @Test
  public void loginRequestAlreadyOnlineThrowsException() {
    when(playerService.isPlayerOnline("JUnit")).thenReturn(true);

    expectedException.expect(requestExceptionWithCode(ErrorCode.USER_ALREADY_CONNECTED));
    instance.loginRequest(new LegacyLoginRequest("JUnit", "password", "uniqueId"), clientConnection);
  }

  @Test
  public void listCoopMissions() {
    instance.listCoopMissions(new ListCoopRequest(), clientConnection);

    verify(clientService).sendCoopList(clientConnection);
  }

  private void createAuthentication(Player player) {
    FafUserDetails fafUserDetails = new FafUserDetails((User) new User()
      .setPlayer(player)
      .setPassword("password")
      .setLogin("JUnit"));
    TestingAuthenticationToken token = new TestingAuthenticationToken(fafUserDetails, "password");
    when(authenticationManager.authenticate(any())).thenReturn(token);
  }
}
