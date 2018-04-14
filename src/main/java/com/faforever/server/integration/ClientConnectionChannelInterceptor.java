package com.faforever.server.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientConnectionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;
import static org.springframework.integration.IntegrationMessageHeaderAccessor.CORRELATION_ID;

/**
 * An interceptor that obtains a {@link ClientConnection} and optional user authentication for a message, based on the
 * client address in the header, and adds both to the message header. Finally, it updates the connection's "last seen"
 * to the current time.
 */
@Component
public class ClientConnectionChannelInterceptor extends ChannelInterceptorAdapter {
  private final ClientConnectionService clientConnectionService;

  public ClientConnectionChannelInterceptor(ClientConnectionService clientConnectionService) {
    this.clientConnectionService = clientConnectionService;
  }

  @Override
  public Message<?> preSend(@NotNull Message<?> message, MessageChannel channel) {
    org.springframework.messaging.MessageHeaders messageHeaders = message.getHeaders();

    String connectionId = (String) messageHeaders.get(CORRELATION_ID);
    ClientConnection clientConnection = clientConnectionService.getClientConnection(connectionId)
      .orElseThrow(() -> new IllegalStateException("There is no connection with ID: " + connectionId));
    clientConnectionService.updateLastSeen(clientConnection, Instant.now());

    SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
    accessor.setUser(clientConnection.getAuthentication());

    return MessageBuilder.fromMessage(message)
      .setHeader(CLIENT_CONNECTION, clientConnection)
      .copyHeaders(accessor.getMessageHeaders())
      .build();
  }
}
