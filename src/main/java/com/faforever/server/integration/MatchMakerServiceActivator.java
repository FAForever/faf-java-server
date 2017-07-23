package com.faforever.server.integration;

import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.entity.Player;
import com.faforever.server.matchmaker.CreateMatchRequest;
import com.faforever.server.matchmaker.MatchMakerCancelRequest;
import com.faforever.server.matchmaker.MatchMakerMapper;
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
  private final MatchMakerMapper matchMakerMapper;

  public MatchMakerServiceActivator(MatchMakerService matchMakerService, MatchMakerMapper matchMakerMapper) {
    this.matchMakerService = matchMakerService;
    this.matchMakerMapper = matchMakerMapper;
  }

  @ServiceActivator(inputChannel = ChannelNames.MATCH_MAKER_SEARCH_REQUEST)
  public void startSearch(MatchMakerSearchRequest request, @Header(USER_HEADER) Authentication authentication) {
    matchMakerService.submitSearch(
      getPlayer(authentication),
      request.getFaction(),
      request.getPoolName()
    );
  }

  @ServiceActivator(inputChannel = ChannelNames.MATCH_MAKER_CANCEL_REQUEST)
  public void cancelSearch(MatchMakerCancelRequest request, @Header(USER_HEADER) Authentication authentication) {
    matchMakerService.cancelSearch(request.getPoolName(), getPlayer(authentication));
  }

  private Player getPlayer(@Header(USER_HEADER) Authentication authentication) {
    return ((FafUserDetails) authentication.getPrincipal()).getPlayer();
  }

  @ServiceActivator(inputChannel = ChannelNames.CLIENT_DISCONNECTED_EVENT)
  public void onClientDisconnected(ClientDisconnectedEvent event) {
    Optional.ofNullable(event.getClientConnection().getAuthentication())
      .ifPresent(authentication -> matchMakerService.removePlayer(getPlayer(authentication)));
  }

  @ServiceActivator(inputChannel = ChannelNames.CREATE_MATCH_REQUEST)
  public void createMatch(CreateMatchRequest request, @Header(USER_HEADER) Authentication authentication) {
    matchMakerService.createMatch(
      getRequester(authentication),
      request.getRequestId(),
      request.getTitle(),
      request.getFeaturedMod(),
      matchMakerMapper.map(request.getParticipants()),
      request.getMapVersionId()
    );
  }

  private ConnectionAware getRequester(Authentication authentication) {
    return (ConnectionAware) authentication.getPrincipal();
  }
}
