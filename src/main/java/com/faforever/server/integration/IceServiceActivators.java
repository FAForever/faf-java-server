package com.faforever.server.integration;

import com.faforever.server.ice.IceMessage;
import com.faforever.server.ice.IceServersRequest;
import com.faforever.server.ice.IceService;
import com.faforever.server.security.FafUserDetails;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.Authentication;

import static com.faforever.server.integration.MessageHeaders.USER_HEADER;

/**
 * Message endpoint that takes ICE (Interactive Connectivity Establishment) messages and calls the respective methods on
 * the {@link IceService}.
 */
@MessageEndpoint
public class IceServiceActivators {
  private final IceService iceService;

  public IceServiceActivators(IceService iceService) {
    this.iceService = iceService;
  }

  @ServiceActivator(inputChannel = ChannelNames.ICE_SERVERS_REQUEST)
  public void requestIceServers(IceServersRequest request, @Header(USER_HEADER) Authentication authentication) {
    iceService.requestIceServers(((FafUserDetails) authentication.getPrincipal()).getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.ICE_MESSAGE)
  public void forwardIceMessage(IceMessage message, @Header(USER_HEADER) Authentication authentication) {
    iceService.forwardIceMessage(((FafUserDetails) authentication.getPrincipal()).getPlayer(), message.getReceiverId(), message.getContent());
  }
}
