package com.faforever.server.config.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientConnectionManager;
import com.faforever.server.client.CloseConnectionEvent;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.integration.MessageHeaders;
import com.faforever.server.integration.Protocol;
import com.faforever.server.integration.legacy.transformer.LegacyRequestTransformer;
import com.faforever.server.integration.legacy.transformer.LegacyResponseTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.HeaderEnricherSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.TcpConnectionCloseEvent;
import org.springframework.integration.ip.tcp.connection.TcpConnectionOpenEvent;
import org.springframework.integration.ip.tcp.connection.TcpNioConnection;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.inject.Inject;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Configuration
@Slf4j
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
   * @see ChannelNames#LEGACY_INBOUND
   */
  @Bean(name = ChannelNames.LEGACY_INBOUND)
  public MessageChannel legacyInbound() {
    return MessageChannels.executor(Executors.newFixedThreadPool(1, r -> new Thread(r, "legacy-in"))).get();
  }

  /**
   * TCP inbound adapter that accepts connections and messages from clients.
   */
  @Bean
  public TcpReceivingChannelAdapter tcpReceivingChannelAdapter() {
    TcpReceivingChannelAdapter tcpReceivingChannelAdapter = new TcpReceivingChannelAdapter();
    tcpReceivingChannelAdapter.setConnectionFactory(tcpServerConnectionFactory());
    tcpReceivingChannelAdapter.setOutputChannel(legacyInbound());
    tcpReceivingChannelAdapter.setErrorChannelName(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME);
    return tcpReceivingChannelAdapter;
  }

  /**
   * Message handler which sends messages to a connected client.
   */
  @Bean
  public TcpSendingMessageHandler tcpSendingMessageHandler() {
    TcpSendingMessageHandler handler = new TcpSendingMessageHandler();
    handler.setConnectionFactory(tcpServerConnectionFactory());
    handler.setTaskScheduler(tcpSendingTaskScheduler());
    handler.setStatsEnabled(true);
    return handler;
  }

  /**
   * Non-blocking TCP connection factory that deserializes into byte array messages.
   */
  @Bean
  public TcpNioServerConnectionFactory tcpServerConnectionFactory() {
    ByteArrayLengthHeaderSerializer serializer = new ByteArrayLengthHeaderSerializer();
    serializer.setMaxMessageSize(100 * 1024);
    serializer.setApplicationEventPublisher(applicationEventPublisher);

    TcpNioServerConnectionFactory connectionFactory = new TcpNioServerConnectionFactory(serverProperties.getPort());
    connectionFactory.setDeserializer(serializer);
    connectionFactory.setSerializer(serializer);
    connectionFactory.setUsingDirectBuffers(true);
    connectionFactory.getMapper().setApplySequence(true);

    AtomicInteger threadCount = new AtomicInteger();
    connectionFactory.setTaskExecutor(new ThreadPoolExecutor(
      8, 8, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(serverProperties.getMessaging().getLegacyAdapterInboundQueueSize()),
      (Runnable r) -> new Thread(r, "legacy-tcp-" + threadCount.incrementAndGet())
    ));
    return connectionFactory;
  }

  /**
   * Integration flow that reads from the TCP inbound gateway and transforms legacy messages into internal messages
   * before writing them into the {@link ChannelNames#CLIENT_INBOUND} channel.
   */
  @Bean
  public IntegrationFlow legacyAdapterInboundFlow(ObjectMapper objectMapper) {
    return IntegrationFlows
      .from(tcpReceivingChannelAdapter())
      .wireTap(this::loggerFlow)
      .transform(legacyByteArrayToStringTransformer())
      .wireTap(this::loggerFlow)
      .transform(Transformers.fromJson(HashMap.class))
      .transform(new LegacyRequestTransformer(objectMapper))
      .channel(ChannelNames.CLIENT_INBOUND)
      .get();
  }

  /**
   * Integration flow that reads from the {@link ChannelNames#LEGACY_OUTBOUND} channel and converts an internal message
   * into the legacy message format and sends it back to the original client.
   */
  @Bean
  public IntegrationFlow legacyAdapterOutboundFlow() {
    return IntegrationFlows
      .from(ChannelNames.LEGACY_OUTBOUND)
      .transform(LegacyResponseTransformer.INSTANCE)
      .transform(Transformers.toJson())
      .transform(stringToLegacyByteArrayTransformer())
      .split(broadcastSplitter())
      .enrichHeaders(connectionIdEnricher())
      .handle(tcpSendingMessageHandler())
      .get();
  }

  private TaskScheduler tcpSendingTaskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setThreadNamePrefix("tcp-legacy-out");
    scheduler.setPoolSize(1);
    return scheduler;
  }

  @EventListener
  public void onConnectionOpened(TcpConnectionOpenEvent event) {
    if (Objects.equals(tcpServerConnectionFactory().getComponentName(), event.getConnectionFactoryName())) {
      TcpNioConnection connection = (TcpNioConnection) event.getSource();
      InetAddress inetAddress = connection.getSocketInfo().getInetAddress();
      clientConnectionManager.createClientConnection(event.getConnectionId(), Protocol.LEGACY_UTF_16, inetAddress);
    }
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

  private void loggerFlow(IntegrationFlowDefinition<?> flow) {
    flow.handle(message -> log.trace("Incoming {}: {}", message));
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

        return clientConnectionManager.getConnections().stream()
          .filter(clientConnection -> clientConnection.getProtocol() == Protocol.LEGACY_UTF_16)
          .map(clientConnection -> MessageBuilder.fromMessage(message)
            .setHeader(MessageHeaders.CLIENT_CONNECTION, clientConnection)
          )
          .collect(Collectors.toList());
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
   * Extracts the connection ID from the {@link ClientConnection} header and sets it as {@link
   * IpHeaders#CONNECTION_ID}.
   */
  private Consumer<HeaderEnricherSpec> connectionIdEnricher() {
    return headerEnricherSpec -> headerEnricherSpec.headerFunction(IpHeaders.CONNECTION_ID,
      message -> message.getHeaders().get(MessageHeaders.CLIENT_CONNECTION, ClientConnection.class).getId());
  }
}
