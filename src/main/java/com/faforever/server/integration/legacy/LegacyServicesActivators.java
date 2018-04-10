package com.faforever.server.integration.legacy;

import com.faforever.server.chat.ChatService;
import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.client.ClientService;
import com.faforever.server.client.LegacyLoginRequest;
import com.faforever.server.client.LegacySessionRequest;
import com.faforever.server.client.ListCoopRequest;
import com.faforever.server.client.SessionResponse;
import com.faforever.server.entity.Player;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.RequestException;
import com.faforever.server.error.Requests;
import com.faforever.server.geoip.GeoIpService;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.player.PlayerService;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.UniqueIdService;
import com.google.common.annotations.VisibleForTesting;
import com.faforever.server.stats.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;


@MessageEndpoint
@Slf4j
public class LegacyServicesActivators {

  private final AuthenticationManager authenticationManager;
  private final ClientService clientService;
  private final UniqueIdService uniqueIdService;
  private final GeoIpService geoIpService;
  private final PlayerService playerService;
  private final ChatService chatService;
  private final CounterService counterService;

  @Inject
  public LegacyServicesActivators(AuthenticationManager authenticationManager, ClientService clientService,
                                  UniqueIdService uniqueIdService, GeoIpService geoIpService,
                                  PlayerService playerService, ChatService chatService,
                                  CounterService counterService) {
    this.authenticationManager = authenticationManager;
    this.clientService = clientService;
    this.uniqueIdService = uniqueIdService;
    this.geoIpService = geoIpService;
    this.playerService = playerService;
    this.chatService = chatService;
    this.counterService = counterService;
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_SESSION_REQUEST, outputChannel = ChannelNames.CLIENT_OUTBOUND)
  public SessionResponse askSession(LegacySessionRequest sessionRequest, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    // TODO this method shouldn't do anything but call a service
    String userAgent = sessionRequest.getUserAgent();
    clientConnection.setUserAgent(userAgent);
    counterService.increment(String.format(Metrics.CLIENTS_USER_AGENT_FORMAT, userAgent));
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

      clientConnection.setAuthentication(authentication);
      Player player = userDetails.getPlayer();
      player.setClientConnection(clientConnection);
      geoIpService.lookupCountryCode(clientConnection.getClientAddress()).ifPresent(player::setCountry);
      geoIpService.lookupTimezone(clientConnection.getClientAddress()).ifPresent(player::setTimeZone);

      uniqueIdService.verify(player, loginRequest.getUniqueId());
      playerService.setPlayerOnline(player);
    } catch (BadCredentialsException e) {
      throw new RequestException(e, ErrorCode.INVALID_LOGIN);
    }
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_COOP_LIST)
  public void listCoopMissions(ListCoopRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    clientService.sendCoopList(clientConnection);
  }

  @ServiceActivator(inputChannel = ChannelNames.CLIENT_DISCONNECTED_EVENT)
  public void onClientDisconnected(ClientDisconnectedEvent event) {
    counterService.decrement(String.format(Metrics.CLIENTS_USER_AGENT_FORMAT, event.getClientConnection().getUserAgent()));
  }
}
