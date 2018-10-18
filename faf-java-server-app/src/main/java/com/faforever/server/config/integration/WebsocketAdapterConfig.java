package com.faforever.server.config.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientConnectionService;
import com.faforever.server.client.CloseConnectionEvent;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.integration.MessageHeaders;
import com.faforever.server.integration.Protocol;
import com.faforever.server.integration.v2.client.V2ClientMessageTransformer;
import com.faforever.server.integration.v2.server.V2ServerMessageTransformer;
import com.faforever.server.player.PlayerService;
import com.faforever.server.security.FafClientDetails;
import com.faforever.server.security.FafUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.HeaderEnricherSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.websocket.IntegrationWebSocketContainer;
import org.springframework.integration.websocket.ServerWebSocketContainer;
import org.springframework.integration.websocket.inbound.WebSocketInboundChannelAdapter;
import org.springframework.integration.websocket.outbound.WebSocketOutboundMessageHandler;
import org.springframework.integration.websocket.support.PassThruSubProtocolHandler;
import org.springframework.integration.websocket.support.SubProtocolHandlerRegistry;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;
import static org.springframework.integration.IntegrationMessageHeaderAccessor.CORRELATION_ID;

@Configuration
@Slf4j
public class WebsocketAdapterConfig {

  private final ClientConnectionService clientConnectionService;

  @Inject
  public WebsocketAdapterConfig(ClientConnectionService clientConnectionService) {
    this.clientConnectionService = clientConnectionService;
  }

  @Bean
  public IntegrationWebSocketContainer serverWebSocketContainer() {
    ServerWebSocketContainer container = new ServerWebSocketContainer("/ws");
    container.setAllowedOrigins("*");
    return container;
  }

  /**
   * WebSocket inbound adapter that accepts connections and messages from clients.
   */
  @Bean
  public WebSocketInboundChannelAdapter webSocketInboundChannelAdapter(IntegrationWebSocketContainer serverWebSocketContainer, PlayerService playerService) {
    WebSocketInboundChannelAdapter adapter = new WebSocketInboundChannelAdapter(serverWebSocketContainer, new SubProtocolHandlerRegistry(new FafSubProtocolHandler(clientConnectionService, playerService)));
    adapter.setErrorChannelName(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME);
    return adapter;
  }

  /**
   * TCP inbound adapter that accepts connections and messages from clients.
   */
  @Bean
  public WebSocketOutboundMessageHandler webSocketOutboundMessageHandler(IntegrationWebSocketContainer serverWebSocketContainer) {
    return new WebSocketOutboundMessageHandler(serverWebSocketContainer);
  }

  /**
   * Integration flow that reads from the TCP inbound gateway and transforms legacy messages into internal messages.
   */
  @Bean
  public IntegrationFlow webSocketAdapterInboundFlow(WebSocketInboundChannelAdapter webSocketInboundChannelAdapter, V2ClientMessageTransformer v2ClientMessageTransformer) {
    return IntegrationFlows
      .from(webSocketInboundChannelAdapter)
      .enrichHeaders(connectionIdEnricher())
      .enrichHeaders(clientConnectionEnricher())
      .transform(v2ClientMessageTransformer)
      .channel(ChannelNames.CLIENT_INBOUND)
      .get();
  }

  /**
   * Integration flow that converts an internal message into the legacy message format and sends it back to the original
   * client.
   */
  @Bean
  public IntegrationFlow webSocketAdapterOutboundFlow(WebSocketOutboundMessageHandler webSocketOutboundMessageHandler, V2ServerMessageTransformer v2ServerMessageTransformer) {
    return IntegrationFlows
      .from(ChannelNames.WEB_OUTBOUND)
      .transform(v2ServerMessageTransformer)
      .split(broadcastSplitter())
      // Handle each message in a single task so that one failing message does not prevent others from being sent.
      // A message may fail if the receiving client is no longer connected
      .channel(ChannelNames.WEB_SOCKET_OUTBOUND)
      .enrichHeaders(sessionIdEnricher())
      .handle(webSocketOutboundMessageHandler)
      .get();
  }

  @EventListener
  public void onCloseConnection(CloseConnectionEvent event) {
    if (event.getClientConnection().getProtocol() == Protocol.V2_JSON_UTF_8) {
      // FIXME implement
      log.warn("Closing websocket connections has not yet been implemented");
    }
  }

  /**
   * Splits messages into per-connection messages if the "broadcast" header is set. Each message get the respective
   * client connection set in its header.
   */
  private AbstractMessageSplitter broadcastSplitter() {
    return new AbstractMessageSplitter() {
      @Override
      protected Object splitMessage(Message<?> message) {
        if (!message.getHeaders().containsKey(MessageHeaders.BROADCAST)) {
          return message;
        }

        return clientConnectionService.getConnections().stream()
          .filter(clientConnection -> clientConnection.getProtocol() == Protocol.V2_JSON_UTF_8)
          .map(clientConnection -> MessageBuilder.fromMessage(message)
            .setHeader(CLIENT_CONNECTION, clientConnection)
          )
          .collect(Collectors.toList());
      }
    };
  }

  /**
   * Extracts the websocket session ID from the message headers and sets it as {@link
   * IntegrationMessageHeaderAccessor#CORRELATION_ID}.
   */
  private Consumer<HeaderEnricherSpec> connectionIdEnricher() {
    return headerEnricherSpec -> headerEnricherSpec.headerFunction(CORRELATION_ID,
      message -> message.getHeaders().get(MessageHeaders.WS_SESSION_ID));
  }

  /**
   * Looks up the {@link ClientConnection} associated with the correlation ID and and sets it as {@link
   * MessageHeaders#CLIENT_CONNECTION}.
   */
  private Consumer<HeaderEnricherSpec> clientConnectionEnricher() {
    return headerEnricherSpec -> headerEnricherSpec.headerFunction(CLIENT_CONNECTION,
      message -> {
        String connectionId = (String) message.getHeaders().get(CORRELATION_ID);
        return clientConnectionService.getClientConnection(connectionId)
          .orElseThrow(() -> new IllegalStateException("There is no connection with ID: " + connectionId));
      });
  }

  /**
   * Extracts the connection ID from the {@link ClientConnection} header and sets it as {@link
   * MessageHeaders#WS_SESSION_ID}. This is required in order for the the websocket adapter to know to which session the
   * message needs to be sent to.
   */
  private Consumer<HeaderEnricherSpec> sessionIdEnricher() {
    return headerEnricherSpec -> headerEnricherSpec.headerFunction(MessageHeaders.WS_SESSION_ID,
      message -> message.getHeaders().get(CLIENT_CONNECTION, ClientConnection.class).getId());
  }

  private static class FafSubProtocolHandler extends PassThruSubProtocolHandler {

    private final ClientConnectionService clientConnectionService;
    private final PlayerService playerService;

    private FafSubProtocolHandler(ClientConnectionService clientConnectionService, PlayerService playerService) {
      this.clientConnectionService = clientConnectionService;
      this.playerService = playerService;

      setSupportedProtocols(Protocol.V2_JSON_UTF_8.name());
    }

    @Override
    public void afterSessionStarted(WebSocketSession session, MessageChannel outputChannel) {
      ClientConnection clientConnection = clientConnectionService.createClientConnection(session.getId(), Protocol.V2_JSON_UTF_8, session.getRemoteAddress().getAddress());

      Principal sessionPrincipal = session.getPrincipal();

      extractClientDetailsOrNull(sessionPrincipal)
        .ifPresent(clientDetails -> clientDetails.setClientConnection(clientConnection));

      if (!(sessionPrincipal instanceof Authentication)) {
        throw new IllegalStateException("Session principal needs to be a subclass of Authentication");
      }
      clientConnection.setAuthentication((Authentication) sessionPrincipal);

      extractUserDetailsOrNull(sessionPrincipal)
        .ifPresent(userDetails -> {
          userDetails.setClientConnection(clientConnection);
          playerService.setPlayerOnline(userDetails.getPlayer());
        });
    }

    private Optional<FafClientDetails> extractClientDetailsOrNull(Principal principal) {
      if (!(principal instanceof OAuth2Authentication)) {
        return Optional.empty();
      }
      Object oAuthPrincipal = ((OAuth2Authentication) principal).getPrincipal();
      if (oAuthPrincipal instanceof FafClientDetails) {
        return Optional.of((FafClientDetails) oAuthPrincipal);
      }
      return Optional.empty();
    }

    private Optional<FafUserDetails> extractUserDetailsOrNull(Principal principal) {
      if (principal instanceof OAuth2Authentication) {
        Object oAuthPrincipal = ((OAuth2Authentication) principal).getPrincipal();
        if (oAuthPrincipal instanceof FafUserDetails) {
          return Optional.of((FafUserDetails) oAuthPrincipal);
        }
      } else if (principal instanceof UsernamePasswordAuthenticationToken) {
        Object tokenPrincipal = ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        if (tokenPrincipal instanceof FafUserDetails) {
          return Optional.of((FafUserDetails) tokenPrincipal);
        }
      }
      return Optional.empty();
    }

    @Override
    public void afterSessionEnded(WebSocketSession session, CloseStatus closeStatus, MessageChannel outputChannel) {
      clientConnectionService.removeConnection(session.getId(), Protocol.V2_JSON_UTF_8);
    }
  }
}
