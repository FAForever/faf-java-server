package com.faforever.server.integration.legacy;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientService;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.client.ListCoopRequest;
import com.faforever.server.client.LoginMessage;
import com.faforever.server.client.SessionResponse;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.RequestException;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.UniqueIdService;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.inject.Inject;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;


@MessageEndpoint
public class LegacyServicesActivators {

  private final AuthenticationManager authenticationManager;
  private final ClientService clientService;
  private final UniqueIdService uniqueIdService;

  @Inject
  public LegacyServicesActivators(AuthenticationManager authenticationManager, ClientService clientService, UniqueIdService uniqueIdService) {
    this.authenticationManager = authenticationManager;
    this.clientService = clientService;
    this.uniqueIdService = uniqueIdService;
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_SESSION_REQUEST, outputChannel = ChannelNames.CLIENT_OUTBOUND)
  public SessionResponse askSession() {
    return SessionResponse.INSTANCE;
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_LOGIN_REQUEST)
  public void loginRequest(LoginMessage loginRequest, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {

    try {
      UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword());
      token.setDetails((ConnectionAware) () -> clientConnection);

      Authentication authentication = authenticationManager.authenticate(token);
      FafUserDetails userDetails = (FafUserDetails) authentication.getPrincipal();

      clientConnection.setUserDetails(userDetails);
      userDetails.getPlayer().setClientConnection(clientConnection);

      uniqueIdService.verify(userDetails.getPlayer(), loginRequest.getUniqueId());

      clientService.sendUserDetails(userDetails, clientConnection.getUserDetails().getPlayer());
    } catch (BadCredentialsException e) {
      throw new RequestException(ErrorCode.INVALID_LOGIN);
    }
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_COOP_LIST)
  public void listCoopMissions(ListCoopRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    clientService.sendCoopList(clientConnection);
  }
}
