package com.faforever.server.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.ice.IceMessage;
import com.faforever.server.ice.IceServersRequest;
import com.faforever.server.ice.IceService;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;

@MessageEndpoint
public class IceServiceActivators {
  private final IceService iceService;

  public IceServiceActivators(IceService iceService) {
    this.iceService = iceService;
  }

  @ServiceActivator(inputChannel = ChannelNames.ICE_SERVERS_REQUEST)
  public void requestIceServers(IceServersRequest request, @Header(CLIENT_CONNECTION) ClientConnection sender) {
    iceService.requestIceServers(sender.getUserDetails().getPlayer());
  }

  @ServiceActivator(inputChannel = ChannelNames.ICE_MESSAGE)
  public void forwardIceMessage(IceMessage message, @Header(CLIENT_CONNECTION) ClientConnection sender) {
    iceService.forwardIceMessage(sender.getUserDetails().getPlayer(), message.getReceiverId(), message.getContent());
  }
}
