package com.faforever.server.integration;

import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.matchmaker.MatchMakerCancelRequest;
import com.faforever.server.matchmaker.MatchMakerSearchRequest;
import com.faforever.server.matchmaker.MatchMakerService;
import com.faforever.server.security.FafUserDetails;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static com.faforever.server.integration.MessageHeaders.USER_HEADER;

/**
 * Message endpoint that takes match maker messages and calls the respective methods on the {@link MatchMakerService}.
 */
@MessageEndpoint
public class MatchMakerServiceActivator {
  private final MatchMakerService matchMakerService;

  public MatchMakerServiceActivator(MatchMakerService matchMakerService) {
    this.matchMakerService = matchMakerService;
  }

  @ServiceActivator(inputChannel = ChannelNames.MATCH_MAKER_SEARCH_REQUEST)
  public void startSearch(MatchMakerSearchRequest request, @Header(USER_HEADER) Authentication authentication) {
    matchMakerService.submitSearch(
      ((FafUserDetails) authentication.getPrincipal()).getPlayer(),
      request.getFaction(),
      request.getQueueName()
    );
  }

  @ServiceActivator(inputChannel = ChannelNames.MATCH_MAKER_CANCEL_REQUEST)
  public void cancelSearch(MatchMakerCancelRequest request, @Header(USER_HEADER) Authentication authentication) {
    matchMakerService.cancelSearch(request.getQueueName(), ((FafUserDetails) authentication.getPrincipal()).getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.CLIENT_DISCONNECTED_EVENT)
  public void onClientDisconnected(ClientDisconnectedEvent event) {
    Optional.ofNullable(event.getClientConnection().getAuthentication())
      .ifPresent(authentication -> matchMakerService.removePlayer(((FafUserDetails) authentication.getPrincipal()).getPlayer()));
  }
}
