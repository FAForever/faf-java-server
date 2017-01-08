package com.faforever.server.integration.legacy;

import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.RequestException;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.integration.legacy.dto.LoginResponse;
import com.faforever.server.integration.legacy.dto.SessionResponse;
import com.faforever.server.integration.session.ClientConnection;
import com.faforever.server.integration.session.SessionManager;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.LoginRequest;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.inject.Inject;

import static com.faforever.server.integration.session.ClientConnection.CLIENT_CONNECTION;

@MessageEndpoint
public class LegacyServiceActivators {

  private final AuthenticationManager authenticationManager;
  private final SessionManager sessionManager;

  @Inject
  public LegacyServiceActivators(AuthenticationManager authenticationManager, SessionManager sessionManager) {
    this.authenticationManager = authenticationManager;
    this.sessionManager = sessionManager;
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_SESSION_REQUEST, outputChannel = ChannelNames.CLIENT_OUTBOUND)
  public SessionResponse askSession() {
    return SessionResponse.INSTANCE;
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_LOGIN_REQUEST, outputChannel = ChannelNames.CLIENT_OUTBOUND)
  public LoginResponse loginRequest(LoginRequest loginRequest, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    try {
      Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword()));
      FafUserDetails userDetails = (FafUserDetails) authentication.getPrincipal();

      clientConnection.setUserDetails(userDetails);

      return new LoginResponse(userDetails);
    } catch (BadCredentialsException e) {
      throw new RequestException(ErrorCode.INVALID_LOGIN);
    }
  }
}
