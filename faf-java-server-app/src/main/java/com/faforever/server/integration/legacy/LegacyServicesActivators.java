package com.faforever.server.integration.legacy;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientService;
import com.faforever.server.client.LegacyLoginRequest;
import com.faforever.server.client.LegacySessionRequest;
import com.faforever.server.client.ListCoopRequest;
import com.faforever.server.client.SessionResponse;
import com.faforever.server.config.security.FafLockedException;
import com.faforever.server.entity.BanDetails;
import com.faforever.server.entity.Player;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.RequestException;
import com.faforever.server.error.Requests;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.player.PlayerService;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.PolicyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Comparator;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;


@MessageEndpoint
@Slf4j
public class LegacyServicesActivators {

  private final AuthenticationManager authenticationManager;
  private final ClientService clientService;
  private final PolicyService policyService;
  private final PlayerService playerService;

  @Inject
  public LegacyServicesActivators(AuthenticationManager authenticationManager, ClientService clientService,
                                  PolicyService policyService, PlayerService playerService) {
    this.authenticationManager = authenticationManager;
    this.clientService = clientService;
    this.policyService = policyService;
    this.playerService = playerService;
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_SESSION_REQUEST, outputChannel = ChannelNames.CLIENT_OUTBOUND)
  public SessionResponse askSession(LegacySessionRequest sessionRequest, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    // TODO this method shouldn't do anything but call a service
    String userAgent = sessionRequest.getUserAgent();
    clientConnection.setUserAgent(userAgent);

    return SessionResponse.INSTANCE;
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_LOGIN_REQUEST)
  @Transactional
  public void loginRequest(LegacyLoginRequest loginRequest, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    // TODO this method shouldn't do anything but call a service
    Requests.verify(!playerService.isPlayerOnline(loginRequest.getLogin()), ErrorCode.USER_ALREADY_CONNECTED, loginRequest.getLogin());

    log.debug("Processing login request from user: {}", loginRequest.getLogin());
    try {
      UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword());

      Authentication authentication = authenticationManager.authenticate(token);
      FafUserDetails userDetails = (FafUserDetails) authentication.getPrincipal();
      checkBan(userDetails);

      clientConnection.setAuthentication(authentication);
      Player player = userDetails.getPlayer();
      player.setClientConnection(clientConnection);

      // TODO check if "session" is really needed by the policy server
      policyService.verify(player, loginRequest.getUniqueId(), "1");
      playerService.setPlayerOnline(player);
    } catch (FafLockedException e) {
      log.debug("Account locked", e);
      checkBan(e.getFafUserDetails());
      throw new IllegalStateException("This exception should never be reached");
    } catch (BadCredentialsException e) {
      throw new RequestException(e, ErrorCode.INVALID_LOGIN);
    }
  }

  /** Throws an exception if the account has an active ban. */
  private void checkBan(FafUserDetails userDetails) {
    userDetails.getPlayer().getBanDetails().stream()
      .filter(banDetail -> !banDetail.isRevoked() && (banDetail.getExpiresAt() == null || banDetail.getExpiresAt().toInstant().isAfter(Instant.now())))
      .max(Comparator.comparing(BanDetails::getId))
      .ifPresent(banDetails -> {
        if (banDetails.getExpiresAt() != null) {
          throw Requests.exception(ErrorCode.TEMPORARILY_BANNED, banDetails.getReason(), banDetails.getExpiresAt());
        } else {
          throw Requests.exception(ErrorCode.PERMANENTLY_BANNED, banDetails.getReason());
        }
      });
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_COOP_LIST)
  public void listCoopMissions(ListCoopRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    clientService.sendCoopList(clientConnection);
  }
}
