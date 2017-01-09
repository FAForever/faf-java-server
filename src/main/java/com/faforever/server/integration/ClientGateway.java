package com.faforever.server.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.response.ServerResponse;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;

import static com.faforever.server.client.ClientConnection.CLIENT_CONNECTION;

/**
 * A Spring Integration gateway to send messages to the client.
 */
@MessagingGateway(defaultRequestChannel = ChannelNames.CLIENT_OUTBOUND)
public interface ClientGateway {

  /**
   * Sends the specified message to the client with the specified connection ID.
   */
  @Gateway
  void send(ServerResponse serverResponse, @Header(CLIENT_CONNECTION) ClientConnection clientConnection);
}
