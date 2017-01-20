package com.faforever.server.config;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientConnectionManager;
import com.faforever.server.client.CloseConnectionEvent;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.integration.MessageHeaders;
import com.faforever.server.integration.Protocol;
import com.faforever.server.integration.legacy.transformer.LegacyRequestTransformer;
import com.faforever.server.integration.legacy.transformer.LegacyResponseTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.HeaderEnricherSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.TcpConnectionCloseEvent;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;
import static com.faforever.server.integration.MessageHeaders.PROTOCOL;

@Configuration
public class LegacyAdapterConfig {

  private final ServerProperties serverProperties;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final ClientConnectionManager clientConnectionManager;

  @Inject
  public LegacyAdapterConfig(ServerProperties serverProperties, ApplicationEventPublisher applicationEventPublisher, ClientConnectionManager clientConnectionManager) {
    this.serverProperties = serverProperties;
    this.applicationEventPublisher = applicationEventPublisher;
    this.clientConnectionManager = clientConnectionManager;
  }

  /**
   * TCP inbound adapter that accepts connections and messages from clients.
   */
  @Bean
  public TcpReceivingChannelAdapter tcpReceivingChannelAdapter() {
    TcpReceivingChannelAdapter tcpReceivingChannelAdapter = new TcpReceivingChannelAdapter();
    tcpReceivingChannelAdapter.setConnectionFactory(tcpServerConnectionFactory());
    tcpReceivingChannelAdapter.setOutputChannel(new DirectChannel());
    tcpReceivingChannelAdapter.setErrorChannelName(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME);
    return tcpReceivingChannelAdapter;
  }

  /**
   * Message handler which sends messages to a connected client.
   */
  @Bean
  public TcpSendingMessageHandler tcpSendingMessageHandler() {
    TcpSendingMessageHandler tcpSendingMessageHandler = new TcpSendingMessageHandler();
    tcpSendingMessageHandler.setConnectionFactory(tcpServerConnectionFactory());
    tcpSendingMessageHandler.setStatsEnabled(true);
    return tcpSendingMessageHandler;
  }

  /**
   * Non-blocking TCP connection factory that deserializes into byte array messages.
   */
  @Bean
  public TcpNioServerConnectionFactory tcpServerConnectionFactory() {
    ByteArrayLengthHeaderSerializer serializer = new ByteArrayLengthHeaderSerializer();
    serializer.setMaxMessageSize(100 * 1024);
    serializer.setApplicationEventPublisher(applicationEventPublisher);

    TcpNioServerConnectionFactory tcpNioServerConnectionFactory = new TcpNioServerConnectionFactory(serverProperties.getPort());
    tcpNioServerConnectionFactory.setDeserializer(serializer);
    tcpNioServerConnectionFactory.setSerializer(serializer);
    tcpNioServerConnectionFactory.getMapper().setApplySequence(true);
    return tcpNioServerConnectionFactory;
  }

  /**
   * Integration flow that reads from the TCP inbound gateway and transforms legacy messages into internal messages.
   */
  @Bean
  public IntegrationFlow legacyAdapterInboundFlow(ObjectMapper objectMapper) {
    return IntegrationFlows
      .from(tcpReceivingChannelAdapter())
      .transform(legacyByteArrayToStringTransformer())
      .transform(Transformers.fromJson(HashMap.class))
      .transform(new LegacyRequestTransformer(objectMapper))
      .enrichHeaders(ImmutableMap.of(PROTOCOL, Protocol.LEGACY_UTF_16))
      .channel(ChannelNames.CLIENT_INBOUND)
      .get();
  }

  /**
   * Integration flow that converts an internal message into the legacy message format and sends it back to the original
   * client.
   */
  @Bean
  public IntegrationFlow legacyAdapterOutboundFlow() {
    return IntegrationFlows
      .from(ChannelNames.LEGACY_OUTBOUND)
      .transform(new LegacyResponseTransformer())
      .transform(Transformers.toJson())
      .transform(stringToLegacyByteArrayTransformer())
      .split(broadcastSplitter())
      .enrichHeaders(connectionIdEnricher())
      .handle(tcpSendingMessageHandler())
      .get();
  }

  @EventListener
  public void onConnectionClosed(TcpConnectionCloseEvent event) {
    if (Objects.equals(tcpServerConnectionFactory().getComponentName(), event.getConnectionFactoryName())) {
      clientConnectionManager.removeConnection(event.getConnectionId(), Protocol.LEGACY_UTF_16);
    }
  }

  @EventListener
  public void onCloseConnection(CloseConnectionEvent event) {
    if (event.getClientConnection().getProtocol() == Protocol.LEGACY_UTF_16) {
      tcpServerConnectionFactory().closeConnection(event.getClientConnection().getId());
    }
  }

  /**
   * Splits messages into per-connection messages if the "broadcast" header is set.
   */
  private AbstractMessageSplitter broadcastSplitter() {
    return new AbstractMessageSplitter() {
      @Override
      protected Object splitMessage(Message<?> message) {
        if (message.getHeaders().containsKey(MessageHeaders.BROADCAST)) {
          return clientConnectionManager.getConnections().stream()
            .filter(clientConnection -> clientConnection.getProtocol() == Protocol.LEGACY_UTF_16)
            .map(clientConnection -> MessageBuilder.fromMessage(message)
              .setHeader(MessageHeaders.CLIENT_CONNECTION, clientConnection)
            )
            .collect(Collectors.toList());
        }
        return message;
      }
    };
  }

  private GenericTransformer<String, byte[]> stringToLegacyByteArrayTransformer() {
    return source -> {
      int length = 2 * source.length();
      return ByteBuffer.allocate(Integer.BYTES + length)
        .putInt(length)
        .put(source.getBytes(StandardCharsets.UTF_16BE))
        .array();
    };
  }

  private GenericTransformer<byte[], String> legacyByteArrayToStringTransformer() {
    return source -> new String(source, Integer.BYTES, source.length - Integer.BYTES, StandardCharsets.UTF_16BE);
  }

  /**
   * Extracts the connection ID from the {@link ClientConnection} header and sets it as {@link IpHeaders#CONNECTION_ID}.
   */
  private Consumer<HeaderEnricherSpec> connectionIdEnricher() {
    return headerEnricherSpec -> headerEnricherSpec.headerFunction(IpHeaders.CONNECTION_ID,
      message -> message.getHeaders().get(CLIENT_CONNECTION, ClientConnection.class).getId());
  }
}
