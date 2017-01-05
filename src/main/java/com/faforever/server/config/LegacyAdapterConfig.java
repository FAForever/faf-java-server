package com.faforever.server.config;

import com.faforever.server.integration.ChannelNames;
import com.faforever.server.legacyadapter.LegacyRequestTransformer;
import com.faforever.server.legacyadapter.LegacyResponseTransformer;
import com.google.common.collect.ImmutableMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.MessageChannel;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.springframework.messaging.MessageHeaders.REPLY_CHANNEL;

@Configuration
public class LegacyAdapterConfig {

  private final MessageChannel errorChannel;
  private final FafServerProperties fafServerProperties;

  /**
   * @param errorChannel the channel to send any errors to
   */
  @Inject
  public LegacyAdapterConfig(MessageChannel errorChannel, FafServerProperties fafServerProperties) {
    this.errorChannel = errorChannel;
    this.fafServerProperties = fafServerProperties;
  }

  /**
   * TCP inbound gateway that accepts connections from clients.
   */
  @Bean
  public TcpInboundGateway tcpInboundGateway() {
    TcpInboundGateway tcpInboundGateway = new TcpInboundGateway();
    tcpInboundGateway.setConnectionFactory(tcpServerConnectionFactory());
    tcpInboundGateway.setRequestChannel(new DirectChannel());
    tcpInboundGateway.setErrorChannel(errorChannel);
    return tcpInboundGateway;
  }


  /**
   * Non-blocking TCP connection factory that deserializes into byte array messages.
   */
  private TcpNioServerConnectionFactory tcpServerConnectionFactory() {
    ByteArrayLengthHeaderSerializer serializer = new ByteArrayLengthHeaderSerializer();
    serializer.setMaxMessageSize(4096);

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
      .enrichHeaders(ImmutableMap.of("legacy", true))
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
      .route(new HeaderValueRouter(REPLY_CHANNEL))
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
}
