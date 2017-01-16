package com.faforever.server.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.matchmaker.MatchMakerSearchRequest;
import com.faforever.server.matchmaker.MatchMakerService;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

@MessageEndpoint
public class MatchMakerServiceActivator {
  private final MatchMakerService matchMakerService;

  public MatchMakerServiceActivator(MatchMakerService matchMakerService) {
    this.matchMakerService = matchMakerService;
  }

  @ServiceActivator(inputChannel = ChannelNames.MATCH_MAKER_MESSAGE)
  public void startSearch(MatchMakerSearchRequest request, @Header ClientConnection clientConnection) {
    matchMakerService.submitSearch(
      clientConnection.getUserDetails().getPlayer(),
      request.getFaction(),
      request.getQueueName()
    );
  }
}
