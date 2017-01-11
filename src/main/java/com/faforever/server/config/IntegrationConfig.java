package com.faforever.server.config;

import com.faforever.server.avatar.AvatarMessage;
import com.faforever.server.client.ClientConnection;
import com.faforever.server.coop.CoopMissionCompletedReport;
import com.faforever.server.error.ErrorResponse;
import com.faforever.server.error.RequestException;
import com.faforever.server.game.*;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.integration.Protocol;
import com.faforever.server.integration.request.GameStateReport;
import com.faforever.server.integration.request.HostGameRequest;
import com.faforever.server.integration.session.SessionRequest;
import com.faforever.server.integration.session.SessionManager;
import com.faforever.server.security.LoginMessage;
import com.faforever.server.social.AddFriendMessage;
import com.faforever.server.social.SocialRemoveMessage;
import com.faforever.server.statistics.ArmyStatisticsReport;
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

import static com.faforever.server.client.ClientConnection.CLIENT_CONNECTION;
import static org.springframework.integration.IntegrationMessageHeaderAccessor.CORRELATION_ID;

@Configuration
@IntegrationComponentScan("com.faforever.server.integration")
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
        switch (message.getHeaders().get(CLIENT_CONNECTION, ClientConnection.class).getProtocol()) {
          case LEGACY_UTF_16:
            return Collections.singletonList(ChannelNames.LEGACY_OUTBOUND);
          default:
            throw new UnsupportedOperationException("Only legacy outbound route is currently specified");
        }
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
    payloadTypeRouter.setChannelMapping(GameStateReport.class.getName(), ChannelNames.UPDATE_GAME_STATE_REQUEST);
    payloadTypeRouter.setChannelMapping(GameOptionReport.class.getName(), ChannelNames.GAME_OPTION_REQUEST);
    payloadTypeRouter.setChannelMapping(PlayerOptionReport.class.getName(), ChannelNames.PLAYER_OPTION_REQUEST);
    payloadTypeRouter.setChannelMapping(ClearSlotRequest.class.getName(), ChannelNames.CLEAR_SLOT_REQUEST);
    payloadTypeRouter.setChannelMapping(AiOptionReport.class.getName(), ChannelNames.AI_OPTION_REQUEST);
    payloadTypeRouter.setChannelMapping(DesyncReport.class.getName(), ChannelNames.DESYNC_REPORT);
    payloadTypeRouter.setChannelMapping(GameModsReport.class.getName(), ChannelNames.GAME_MODS_REPORT);
    payloadTypeRouter.setChannelMapping(GameModsCountReport.class.getName(), ChannelNames.GAME_MODS_COUNT_REPORT);
    payloadTypeRouter.setChannelMapping(ArmyScoreReport.class.getName(), ChannelNames.ARMY_SCORE_REPORT);
    payloadTypeRouter.setChannelMapping(ArmyOutcomeReport.class.getName(), ChannelNames.ARMY_OUTCOME_REPORT);
    payloadTypeRouter.setChannelMapping(CoopMissionCompletedReport.class.getName(), ChannelNames.OPERATION_COMPLETE_REPORT);
    payloadTypeRouter.setChannelMapping(ArmyStatisticsReport.class.getName(), ChannelNames.GAME_STATISTICS_REPORT);
    payloadTypeRouter.setChannelMapping(EnforceRatingRequest.class.getName(), ChannelNames.ENFORCE_RATING_REQUEST);
    payloadTypeRouter.setChannelMapping(TeamKillReport.class.getName(), ChannelNames.TEAM_KILL_REPORT);
    payloadTypeRouter.setChannelMapping(LoginMessage.class.getName(), ChannelNames.LEGACY_LOGIN_REQUEST);
    payloadTypeRouter.setChannelMapping(SessionRequest.class.getName(), ChannelNames.LEGACY_SESSION_REQUEST);
    payloadTypeRouter.setChannelMapping(AvatarMessage.class.getName(), ChannelNames.LEGACY_AVATAR_REQUEST);
    payloadTypeRouter.setChannelMapping(AddFriendMessage.class.getName(), ChannelNames.LEGACY_ADD_FRIEND_REQUEST);
    payloadTypeRouter.setChannelMapping(SocialRemoveMessage.class.getName(), ChannelNames.LEGACY_REMOVE_FRIEND_REQUEST);

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
        builder.setHeader(CLIENT_CONNECTION, failedMessage.getHeaders().get(CLIENT_CONNECTION, ClientConnection.class));
        return builder.build();
      }
    };
  }

  /**
   * Adds an existing or new {@link ClientConnection} to the message's header, using the correlationId header as a
   * session ID.
   */
  private Consumer<HeaderEnricherSpec> sessionHeaderEnricher() {
    return headerEnricherSpec -> headerEnricherSpec.messageProcessor(message -> {
      String sessionId = (String) message.getHeaders().get(CORRELATION_ID);
      Protocol protocol = (Protocol) message.getHeaders().get("protocol");
      return ImmutableMap.of(CLIENT_CONNECTION, sessionManager.obtainSession(sessionId, protocol));
    });
  }
}
