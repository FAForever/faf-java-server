package com.faforever.server.integration;

import com.faforever.server.entity.Game;
import com.faforever.server.entity.Player;
import com.faforever.server.integration.response.LaunchGameResponse;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.messaging.handler.annotation.Header;

/**
 * A gateway to send messages to the client.
 */
@MessagingGateway(defaultRequestChannel = ChannelNames.CLIENT_OUTBOUND)
public interface ClientGateway {

  @Gateway
  void joinGame(Game game, Player player);

  @Gateway
  void launchGame(LaunchGameResponse launchGameResponse,
                  @Header(name = IpHeaders.CONNECTION_ID, value = "player.clientConnection.id") Player player);
}
