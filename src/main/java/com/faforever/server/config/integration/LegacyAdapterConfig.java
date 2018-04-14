package com.faforever.server.config.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientConnectionService;
import com.faforever.server.client.CloseConnectionEvent;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.integration.MessageHeaders;
import com.faforever.server.integration.Protocol;
import com.faforever.server.integration.legacy.transformer.LegacyRequestTransformer;
import com.faforever.server.integration.legacy.transformer.LegacyResponseTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.HeaderEnricherSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.TcpConnectionCloseEvent;
import org.springframework.integration.ip.tcp.connection.TcpConnectionExceptionEvent;
import org.springframework.integration.ip.tcp.connection.TcpConnectionOpenEvent;
import org.springframework.integration.ip.tcp.connection.TcpNioConnection;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.integration.util.CompositeExecutor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.inject.Inject;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;
import static org.springframework.integration.IntegrationMessageHeaderAccessor.CORRELATION_ID;

@Configuration
@Slf4j
public class LegacyAdapterConfig {

  private final ServerProperties serverProperties;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final ClientConnectionService clientConnectionService;

  @Inject
  public LegacyAdapterConfig(ServerProperties serverProperties, ApplicationEventPublisher applicationEventPublisher, ClientConnectionService clientConnectionService) {
    this.serverProperties = serverProperties;
    this.applicationEventPublisher = applicationEventPublisher;
    this.clientConnectionService = clientConnectionService;
  }

  /**
   * @see ChannelNames#LEGACY_INBOUND
   */
  @Bean(name = ChannelNames.LEGACY_INBOUND)
  public MessageChannel legacyInbound() {
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
      1,
      1,
      0L, TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue<>(serverProperties.getMessaging().getLegacyAdapterInboundQueueSize()),
      runnable -> new Thread(runnable, "legacy-in"));

    return MessageChannels.executor(threadPoolExecutor).get();
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

    // See https://docs.spring.io/spring-integration/reference/html/ip.html#_thread_pool_task_executor_with_caller_runs_policy
    connectionFactory.setTaskExecutor(new CompositeExecutor(
      createNioTaskExecutor("legacy-io-"),
      createNioTaskExecutor("legacy-assembler-")
    ));

    return connectionFactory;
  }

  @NotNull
  private Executor createNioTaskExecutor(String threadNamePrefix) {
    ThreadPoolTaskExecutor ioExecutor = new ThreadPoolTaskExecutor();
    ioExecutor.setCorePoolSize(1);
    ioExecutor.setMaxPoolSize(4);
    ioExecutor.setQueueCapacity(0);
    ioExecutor.setThreadNamePrefix(threadNamePrefix);
    ioExecutor.setRejectedExecutionHandler(new AbortPolicy());
    ioExecutor.initialize();
    return ioExecutor;
  }

  /**
   * Integration flow that reads from the TCP inbound gateway and transforms legacy messages into internal messages
   * before writing them into the {@link ChannelNames#CLIENT_INBOUND} channel.
   */
  @Bean
  public IntegrationFlow legacyAdapterInboundFlow(ObjectMapper objectMapper) {
    return IntegrationFlows
      .from(tcpReceivingChannelAdapter())
      .enrichHeaders(clientConnectionEnricher())
      .transform(legacyByteArrayToStringTransformer())
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
      // Handle each message in a single task so that one failing message does not prevent others from being sent.
      // A message may fail if the receiving client is no longer connected
      .channel(ChannelNames.LEGACY_TCP_OUTBOUND)
      .enrichHeaders(connectionIdEnricher())
      .handle(tcpSendingMessageHandler())
      .get();
  }

  @EventListener
  public void onConnectionOpened(TcpConnectionOpenEvent event) {
    log.debug("Connection opened: {}", event.getConnectionId());

    if (Objects.equals(tcpServerConnectionFactory().getComponentName(), event.getConnectionFactoryName())) {
      TcpNioConnection connection = (TcpNioConnection) event.getSource();
      InetAddress inetAddress = connection.getSocketInfo().getInetAddress();
      clientConnectionService.createClientConnection(event.getConnectionId(), Protocol.V1_LEGACY_UTF_16, inetAddress);
    }
  }

  @EventListener
  public void onConnectionClosed(TcpConnectionCloseEvent event) {
    log.debug("Connection closed: {}", event.getConnectionId());

    if (Objects.equals(tcpServerConnectionFactory().getComponentName(), event.getConnectionFactoryName())) {
      clientConnectionService.removeConnection(event.getConnectionId(), Protocol.V1_LEGACY_UTF_16);
    }
  }

  @EventListener
  public void onConnectionClosed(TcpConnectionExceptionEvent event) {
    log.debug("Connection exception: {}", event.getConnectionId(), event.getCause());

    if (Objects.equals(tcpServerConnectionFactory().getComponentName(), event.getConnectionFactoryName())) {
      clientConnectionService.removeConnection(event.getConnectionId(), Protocol.V1_LEGACY_UTF_16);
    }
  }

  @EventListener
  public void onCloseConnection(CloseConnectionEvent event) {
    if (event.getClientConnection().getProtocol() == Protocol.V1_LEGACY_UTF_16) {
      tcpServerConnectionFactory().closeConnection(event.getClientConnection().getId());
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
          .filter(clientConnection -> clientConnection.getProtocol() == Protocol.V1_LEGACY_UTF_16)
          .map(clientConnection -> MessageBuilder.fromMessage(message)
            .setHeader(CLIENT_CONNECTION, clientConnection)
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
   * Extracts the connection ID from the {@link ClientConnection} header and sets it as {@link IpHeaders#CONNECTION_ID}.
   * This is required in order for the the TCP adapter to know to which socket the message needs to be sent to.
   */
  private Consumer<HeaderEnricherSpec> connectionIdEnricher() {
    return headerEnricherSpec -> headerEnricherSpec.headerFunction(IpHeaders.CONNECTION_ID,
      message -> message.getHeaders().get(CLIENT_CONNECTION, ClientConnection.class).getId());
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
}
