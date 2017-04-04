package com.faforever.server.integration.legacy;

import com.faforever.server.chat.ChatService;
import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientService;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.client.ListCoopRequest;
import com.faforever.server.client.LoginMessage;
import com.faforever.server.client.SessionResponse;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.geoip.GeoIpService;
import com.faforever.server.integration.Protocol;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.UniqueIdService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.net.InetAddress;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LegacyServicesActivatorsTest {

  private LegacyServicesActivators instance;

  @Mock
  private AuthenticationManager authenticationManager;
  @Mock
  private ClientService clientService;
  @Mock
  private UniqueIdService uniqueIdService;
  @Mock
  private GeoIpService geoIpService;
  @Mock
  private ChatService chatService;

  private ClientConnection clientConnection;
  private Player player;

  @Before
  public void setUp() throws Exception {
    clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class));
    player = new Player();
    player.setClientConnection(clientConnection);

    when(geoIpService.lookupCountryCode(any())).thenReturn(Optional.empty());

    instance = new LegacyServicesActivators(authenticationManager, clientService, uniqueIdService, geoIpService, chatService);
  }

  @Test
  public void askSession() throws Exception {
    SessionResponse sessionResponse = instance.askSession();
    assertThat(sessionResponse, is(SessionResponse.INSTANCE));
  }

  @Test
  public void loginRequest() throws Exception {
    createAuthentication(player);

    instance.loginRequest(new LoginMessage("JUnit", "password", "uniqueid"), clientConnection);

    ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
    verify(authenticationManager).authenticate(captor.capture());
    UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) captor.getValue();

    assertThat(authentication.getPrincipal(), is("JUnit"));
    assertThat(authentication.getCredentials(), is("password"));
    assertThat(authentication.getDetails(), is(instanceOf(ConnectionAware.class)));
    assertThat(((ConnectionAware) authentication.getDetails()).getClientConnection(), is(clientConnection));
  }

  @Test
  public void loginRequestCallsUniqueIdService() throws Exception {
    createAuthentication(new Player());

    instance.loginRequest(new LoginMessage("JUnit", "password", "uniqueid"), clientConnection);

    verify(uniqueIdService).verify(any(), eq("uniqueid"));
  }

  @Test
  public void listCoopMissions() throws Exception {
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
