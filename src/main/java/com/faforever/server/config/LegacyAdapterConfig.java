package com.faforever.server.config;

import com.faforever.server.integration.ChannelNames;
import com.faforever.server.integration.Protocol;
import com.faforever.server.integration.legacy.transformer.LegacyRequestTransformer;
import com.faforever.server.integration.legacy.transformer.LegacyResponseTransformer;
import com.faforever.server.integration.session.ClientConnection;
import com.google.common.collect.ImmutableMap;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.HeaderEnricherSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.transformer.GenericTransformer;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Configuration
public class LegacyAdapterConfig {

  private final FafServerProperties fafServerProperties;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Inject
  public LegacyAdapterConfig(FafServerProperties fafServerProperties, ApplicationEventPublisher applicationEventPublisher) {
    this.fafServerProperties = fafServerProperties;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  /**
   * TCP inbound gateway that accepts connections from clients.
   */
  @Bean
  public TcpInboundGateway tcpInboundGateway() {
    TcpInboundGateway tcpInboundGateway = new TcpInboundGateway();
    tcpInboundGateway.setConnectionFactory(tcpServerConnectionFactory());
    tcpInboundGateway.setRequestChannel(new DirectChannel());
    tcpInboundGateway.setErrorChannelName(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME);
    return tcpInboundGateway;
  }

  /**
   * Message handler which sends messages to one or more connected clients.
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
  private TcpNioServerConnectionFactory tcpServerConnectionFactory() {
    ByteArrayLengthHeaderSerializer serializer = new ByteArrayLengthHeaderSerializer();
    serializer.setMaxMessageSize(100 * 1024);
    serializer.setApplicationEventPublisher(applicationEventPublisher);

    TcpNioServerConnectionFactory tcpNioServerConnectionFactory = new TcpNioServerConnectionFactory(fafServerProperties.getPort());
    tcpNioServerConnectionFactory.setDeserializer(serializer);
    tcpNioServerConnectionFactory.setSerializer(serializer);
    tcpNioServerConnectionFactory.getMapper().setApplySequence(true);
    return tcpNioServerConnectionFactory;
  }

  /**
   * Integration flow that reads from the TCP inbound gateway and transforms legacy messages into internal messages.
   */
  @Bean
  public IntegrationFlow legacyAdapterInboundFlow() {
    return IntegrationFlows
      .from(tcpInboundGateway())
      .transform(legacyByteArrayToStringTransformer())
      .transform(Transformers.fromJson(HashMap.class))
      .transform(new LegacyRequestTransformer())
      .enrichHeaders(ImmutableMap.of("protocol", Protocol.LEGACY_UTF_16))
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
      .enrichHeaders(connectionIdEnricher())
      .get();
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

  private Consumer<HeaderEnricherSpec> connectionIdEnricher() {
    return headerEnricherSpec -> headerEnricherSpec.messageProcessor(message -> {
      if (message.getHeaders().getReplyChannel() == null) {
        return ImmutableMap.of(IpHeaders.CONNECTION_ID, (message.getHeaders().get(ClientConnection.CLIENT_CONNECTION, ClientConnection.class)).getId());
      }
      return ImmutableMap.of();
    });
  }
}
