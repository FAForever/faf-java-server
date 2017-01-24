package com.faforever.server.client;

import com.faforever.server.integration.ChannelNames;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

import javax.inject.Inject;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;

@MessageEndpoint
public class ConnectionServiceActivator {

  private final ClientConnectionManager clientConnectionManager;

  @Inject
  public ConnectionServiceActivator(ClientConnectionManager clientConnectionManager) {
    this.clientConnectionManager = clientConnectionManager;
  }

  @ServiceActivator(inputChannel = ChannelNames.DISCONNECT_CLIENT_REQUEST)
  public void disconnectClientRequest(DisconnectClientRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    clientConnectionManager.disconnectClient(clientConnection.getUserDetails().getUser(), request.getUserId());
  }
}
