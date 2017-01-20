package com.faforever.server.client;

import com.faforever.server.integration.ChannelNames;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

import javax.inject.Inject;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;

@MessageEndpoint
public class ClientServiceActivators {

  private final ClientService clientService;

  @Inject
  public ClientServiceActivators(ClientService clientService) {
    this.clientService = clientService;
  }

  @ServiceActivator(inputChannel = ChannelNames.DISCONNECT_CLIENT_REQUEST)
  public void disconnectClientRequest(DisconnectClientRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    clientService.disconnectClient(clientConnection.getUserDetails().getUser(), request.getUserId());
  }
}
