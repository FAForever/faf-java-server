package com.faforever.server.config;

import com.faforever.server.avatar.AvatarRequest;
import com.faforever.server.error.ErrorResponse;
import com.faforever.server.error.RequestException;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.integration.request.HostGameRequest;
import com.faforever.server.integration.request.JoinGameRequest;
import com.faforever.server.integration.request.UpdateGameStateRequest;
import com.faforever.server.integration.session.AskSessionRequest;
import com.faforever.server.integration.session.Session;
import com.faforever.server.integration.session.SessionManager;
import com.faforever.server.security.LoginRequest;
import com.faforever.server.social.SocialAddRequest;
import com.faforever.server.social.SocialRemoveRequest;
import com.google.common.collect.ImmutableMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.core.GenericSelector;
import org.springframework.integration.dsl.HeaderEnricherSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.integration.router.PayloadTypeRouter;
import org.springframework.integration.security.channel.SecurityContextPropagationChannelInterceptor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.AbstractTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;

import java.util.Collections;
import java.util.List;

import static org.springframework.integration.IntegrationMessageHeaderAccessor.CORRELATION_ID;

@Configuration
@IntegrationComponentScan
public class IntegrationConfig {

  private final SessionManager sessionManager;

  public IntegrationConfig(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  /**
   * Reads messages from the standard client inbound channel.
   */
  @Bean
  public IntegrationFlow inboundFlow() {
    return IntegrationFlows
      .from(ChannelNames.CLIENT_INBOUND)
      .enrichHeaders(sessionHeaderEnricher())
      .route(inboundRouter())
      .get();
  }

  /**
   * Reads messages from the standard outbound channel and sends it to the correct adapter's channel.
   */
  @Bean
  public IntegrationFlow outboundFlow() {
    return IntegrationFlows
      .from(ChannelNames.CLIENT_OUTBOUND)
      .route(outboundRouter())
      .get();
  }

  /**
   * Subscribes to the errorChannel and transforms any {@link RequestException} into and {@link ErrorResponse}.
   */
  @Bean
  public IntegrationFlow errorFlow() {
    return IntegrationFlows
      .from(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME)
      .filter(requestExceptionFilter())
      .transform(requestExceptionTransformer())
      .channel(ChannelNames.CLIENT_OUTBOUND)
      .get();
  }

  @Bean
  @GlobalChannelInterceptor
  public SecurityContextPropagationChannelInterceptor securityContextPropagationChannelInterceptor() {
    return new SecurityContextPropagationChannelInterceptor();
  }

  /**
   * Routes response messages to the appropriate outbound adapter (currently, only legacy adapter is supported).
   */
  private AbstractMessageRouter outboundRouter() {
    return new AbstractMappingMessageRouter() {
      @Override
      protected List<Object> getChannelKeys(Message<?> message) {
        if (message.getHeaders().containsKey("legacy")) {
          return Collections.singletonList(ChannelNames.LEGACY_OUTBOUND);
        }
        throw new UnsupportedOperationException("Only legacy outbound route is specified");
      }
    };
  }

  /**
   * Routes request messages to their corresponding request channel.
   */
  private PayloadTypeRouter inboundRouter() {
    PayloadTypeRouter payloadTypeRouter = new PayloadTypeRouter();
    payloadTypeRouter.setChannelMapping(HostGameRequest.class.getName(), ChannelNames.HOST_GAME_REQUEST);
    payloadTypeRouter.setChannelMapping(JoinGameRequest.class.getName(), ChannelNames.JOIN_GAME_REQUEST);
    payloadTypeRouter.setChannelMapping(UpdateGameStateRequest.class.getName(), ChannelNames.UPDATE_GAME_STATE_REQUEST);
    payloadTypeRouter.setChannelMapping(LoginRequest.class.getName(), ChannelNames.LEGACY_LOGIN_REQUEST);
    payloadTypeRouter.setChannelMapping(AskSessionRequest.class.getName(), ChannelNames.LEGACY_SESSION_REQUEST);
    payloadTypeRouter.setChannelMapping(AvatarRequest.class.getName(), ChannelNames.LEGACY_AVATAR_REQUEST);
    payloadTypeRouter.setChannelMapping(SocialAddRequest.class.getName(), ChannelNames.LEGACY_ADD_FRIEND_REQUEST);
    payloadTypeRouter.setChannelMapping(SocialRemoveRequest.class.getName(), ChannelNames.LEGACY_REMOVE_FRIEND_REQUEST);

    return payloadTypeRouter;
  }

  /**
   * Filter that lets {@link RequestException RequestExceptions} pass only.
   */
  private GenericSelector<Object> requestExceptionFilter() {
    return (Object source) -> {
      if (!(source instanceof MessageHandlingException)) {
        return false;
      }
      MessageHandlingException messagingException = (MessageHandlingException) source;
      return messagingException.getCause() instanceof RequestException;
    };
  }

  /**
   * Transformer that transforms a {@link MessageHandlingException} into and {@link ErrorResponse}.
   */
  private AbstractTransformer requestExceptionTransformer() {
    return new AbstractTransformer() {
      @Override
      protected Object doTransform(Message<?> message) throws Exception {
        MessageHandlingException messageHandlingException = (MessageHandlingException) message.getPayload();
        Message<?> failedMessage = messageHandlingException.getFailedMessage();
        RequestException cause = (RequestException) messageHandlingException.getCause();

        MessageBuilder<ErrorResponse> builder = MessageBuilder.withPayload(new ErrorResponse(cause.getErrorCode()))
          .copyHeaders(message.getHeaders());
        if (failedMessage.getHeaders().containsKey(LegacyAdapterConfig.ORIGIN_HEADER)) {
          builder.setHeader(LegacyAdapterConfig.ORIGIN_HEADER, true);
        }

        return builder.build();
      }
    };
  }

  /**
   * Adds the {@link Session} to the message's header, using the correlationId header as a session ID.
   */
  private Consumer<HeaderEnricherSpec> sessionHeaderEnricher() {
    return headerEnricherSpec -> headerEnricherSpec.messageProcessor(message -> {
      String sessionId = (String) message.getHeaders().get(CORRELATION_ID);
      return ImmutableMap.of(Session.SESSION, sessionManager.obtainSession(sessionId));
    });
  }
}
