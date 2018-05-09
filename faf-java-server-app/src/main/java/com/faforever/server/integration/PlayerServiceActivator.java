package com.faforever.server.integration;

import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.player.PlayerService;
import com.faforever.server.security.FafUserDetails;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import java.util.Optional;

/**
 * Message endpoint that calls {@link PlayerServiceActivator}.
 */
@MessageEndpoint
public class PlayerServiceActivator {

  private final PlayerService playerService;

  public PlayerServiceActivator(PlayerService playerService) {
    this.playerService = playerService;
  }

  @ServiceActivator(inputChannel = ChannelNames.CLIENT_DISCONNECTED_EVENT)
  public void onClientDisconnected(ClientDisconnectedEvent event) {
    Optional.ofNullable(event.getClientConnection().getAuthentication())
      .ifPresent(authentication -> playerService.removePlayer(((FafUserDetails) authentication.getPrincipal()).getPlayer()));
  }
}
