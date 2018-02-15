package com.faforever.server.config.integration;

import com.faforever.server.avatar.ListAvatarsRequest;
import com.faforever.server.avatar.SelectAvatarRequest;
import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.client.DisconnectClientRequest;
import com.faforever.server.client.LegacyLoginRequest;
import com.faforever.server.client.LegacySessionRequest;
import com.faforever.server.client.LoginRequest;
import com.faforever.server.coop.CoopMissionCompletedReport;
import com.faforever.server.error.ErrorResponse;
import com.faforever.server.error.RequestException;
import com.faforever.server.game.AiOptionReport;
import com.faforever.server.game.ArmyOutcomeReport;
import com.faforever.server.game.ArmyScoreReport;
import com.faforever.server.game.BottleneckClearedReport;
import com.faforever.server.game.BottleneckReport;
import com.faforever.server.game.ClearSlotRequest;
import com.faforever.server.game.DesyncReport;
import com.faforever.server.game.DisconnectPeerRequest;
import com.faforever.server.game.DisconnectedReport;
import com.faforever.server.game.GameModsCountReport;
import com.faforever.server.game.GameModsReport;
import com.faforever.server.game.GameOptionReport;
import com.faforever.server.game.GameStateReport;
import com.faforever.server.game.HostGameRequest;
import com.faforever.server.game.JoinGameRequest;
import com.faforever.server.game.MutuallyAgreedDrawRequest;
import com.faforever.server.game.PlayerDefeatedReport;
import com.faforever.server.game.PlayerOptionReport;
import com.faforever.server.game.TeamKillReport;
import com.faforever.server.ice.IceMessage;
import com.faforever.server.ice.IceServersRequest;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.integration.legacy.transformer.RestoreGameSessionRequest;
import com.faforever.server.matchmaker.CreateMatchRequest;
import com.faforever.server.matchmaker.MatchMakerCancelRequest;
import com.faforever.server.matchmaker.MatchMakerSearchRequest;
import com.faforever.server.social.AddFoeRequest;
import com.faforever.server.social.AddFriendRequest;
import com.faforever.server.social.RemoveFoeRequest;
import com.faforever.server.social.RemoveFriendRequest;
import com.faforever.server.stats.ArmyStatisticsReport;
import com.google.common.collect.ImmutableMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.core.GenericSelector;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.event.inbound.ApplicationEventListeningMessageProducer;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.integration.router.PayloadTypeRouter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.AbstractTransformer;
import org.springframework.integration.transformer.MessageTransformationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.MessagingException;

import java.util.Collections;
import java.util.List;

import static com.faforever.server.integration.MessageHeaders.BROADCAST;
import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;

@Configuration
@IntegrationComponentScan("com.faforever.server.integration")
public class IntegrationConfig {

  /**
   * Reads messages from the standard client inbound channel. Since messages are resequenced, all messages must specify
   * the header {@link IntegrationMessageHeaderAccessor#SEQUENCE_NUMBER}.
   */
  @Bean
  public IntegrationFlow inboundFlow() {
    return IntegrationFlows
      .from(ChannelNames.CLIENT_INBOUND)
      // FIXME websocket messages don't have a sequence number yet
//      .resequence(spec -> spec.releasePartialSequences(true))
      .channel(ChannelNames.INBOUND_DISPATCH)
      .get();
  }

  @Bean
  public IntegrationFlow dispatchFlow() {
    return IntegrationFlows
      .from(ChannelNames.INBOUND_DISPATCH)
      .route(inboundRouter())
      .get();
  }

  /**
   * Reads messages from the client outbound channel and sends it to the target adapter's channel.
   */
  @Bean
  public IntegrationFlow outboundFlow() {
    return IntegrationFlows
      .from(ChannelNames.CLIENT_OUTBOUND)
      .route(outboundAdapterRouter())
      .get();
  }

  /**
   * Reads messages from the broadcast channel, enriches them with a "broadcast" header and routes it to all adapter
   * channels.
   */
  @Bean
  public IntegrationFlow broadcastFlow() {
    return IntegrationFlows
      .from(ChannelNames.CLIENT_OUTBOUND_BROADCAST)
      .enrichHeaders(ImmutableMap.of(BROADCAST, true))
      .routeToRecipients(spec -> spec
        // Add more channels here as more protocols come available
        .recipient(ChannelNames.LEGACY_OUTBOUND)
        .recipient(ChannelNames.WEB_OUTBOUND)
      )
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

  /**
   * Turns specific application events into messages.
   */
  @Bean
  public ApplicationEventListeningMessageProducer applicationEventListeningMessageProducer() {
    ApplicationEventListeningMessageProducer producer = new ApplicationEventListeningMessageProducer();
    producer.setEventTypes(
      ClientDisconnectedEvent.class
    );
    producer.setOutputChannelName(ChannelNames.INBOUND_DISPATCH);
    return producer;
  }

  /**
   * Routes response messages to the appropriate outbound adapter.
   */
  private AbstractMessageRouter outboundAdapterRouter() {
    return new AbstractMappingMessageRouter() {
      @Override
      protected List<Object> getChannelKeys(Message<?> message) {
        switch (message.getHeaders().get(CLIENT_CONNECTION, ClientConnection.class).getProtocol()) {
          case V1_LEGACY_UTF_16:
            return Collections.singletonList(ChannelNames.LEGACY_OUTBOUND);
          case V2_JSON_UTF_8:
            return Collections.singletonList(ChannelNames.WEB_OUTBOUND);
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
    PayloadTypeRouter router = new PayloadTypeRouter();
    router.setChannelMapping(HostGameRequest.class.getName(), ChannelNames.HOST_GAME_REQUEST);
    router.setChannelMapping(JoinGameRequest.class.getName(), ChannelNames.JOIN_GAME_REQUEST);
    router.setChannelMapping(GameStateReport.class.getName(), ChannelNames.UPDATE_GAME_STATE_REQUEST);
    router.setChannelMapping(GameOptionReport.class.getName(), ChannelNames.GAME_OPTION_REQUEST);
    router.setChannelMapping(PlayerOptionReport.class.getName(), ChannelNames.PLAYER_OPTION_REQUEST);
    router.setChannelMapping(ClearSlotRequest.class.getName(), ChannelNames.CLEAR_SLOT_REQUEST);
    router.setChannelMapping(AiOptionReport.class.getName(), ChannelNames.AI_OPTION_REQUEST);
    router.setChannelMapping(DesyncReport.class.getName(), ChannelNames.DESYNC_REPORT);
    router.setChannelMapping(GameModsReport.class.getName(), ChannelNames.GAME_MODS_REPORT);
    router.setChannelMapping(GameModsCountReport.class.getName(), ChannelNames.GAME_MODS_COUNT_REPORT);
    router.setChannelMapping(ArmyScoreReport.class.getName(), ChannelNames.ARMY_SCORE_REPORT);
    router.setChannelMapping(ArmyOutcomeReport.class.getName(), ChannelNames.ARMY_OUTCOME_REPORT);
    router.setChannelMapping(CoopMissionCompletedReport.class.getName(), ChannelNames.OPERATION_COMPLETE_REPORT);
    router.setChannelMapping(ArmyStatisticsReport.class.getName(), ChannelNames.GAME_STATISTICS_REPORT);
    router.setChannelMapping(PlayerDefeatedReport.class.getName(), ChannelNames.ENFORCE_RATING_REQUEST);
    router.setChannelMapping(TeamKillReport.class.getName(), ChannelNames.TEAM_KILL_REPORT);
    router.setChannelMapping(DisconnectPeerRequest.class.getName(), ChannelNames.DISCONNECT_PEER_REQUEST);
    router.setChannelMapping(DisconnectClientRequest.class.getName(), ChannelNames.DISCONNECT_CLIENT_REQUEST);
    router.setChannelMapping(MatchMakerSearchRequest.class.getName(), ChannelNames.MATCH_MAKER_SEARCH_REQUEST);
    router.setChannelMapping(MatchMakerCancelRequest.class.getName(), ChannelNames.MATCH_MAKER_CANCEL_REQUEST);
    router.setChannelMapping(IceServersRequest.class.getName(), ChannelNames.ICE_SERVERS_REQUEST);
    router.setChannelMapping(IceMessage.class.getName(), ChannelNames.ICE_MESSAGE);
    router.setChannelMapping(RestoreGameSessionRequest.class.getName(), ChannelNames.RESTORE_GAME_SESSION_REQUEST);
    router.setChannelMapping(MutuallyAgreedDrawRequest.class.getName(), ChannelNames.MUTUALLY_AGREED_DRAW_REQUEST);
    router.setChannelMapping(ClientDisconnectedEvent.class.getName(), ChannelNames.CLIENT_DISCONNECTED_EVENT);
    router.setChannelMapping(ListAvatarsRequest.class.getName(), ChannelNames.LIST_AVATAR);
    router.setChannelMapping(SelectAvatarRequest.class.getName(), ChannelNames.SELECT_AVATAR);
    router.setChannelMapping(LoginRequest.class.getName(), ChannelNames.LOGIN_REQUEST);
    router.setChannelMapping(CreateMatchRequest.class.getName(), ChannelNames.CREATE_MATCH_REQUEST);
    router.setChannelMapping(LegacyLoginRequest.class.getName(), ChannelNames.LEGACY_LOGIN_REQUEST);
    router.setChannelMapping(LegacySessionRequest.class.getName(), ChannelNames.LEGACY_SESSION_REQUEST);
    router.setChannelMapping(AddFriendRequest.class.getName(), ChannelNames.ADD_FRIEND_REQUEST);
    router.setChannelMapping(AddFoeRequest.class.getName(), ChannelNames.ADD_FOE_REQUEST);
    router.setChannelMapping(RemoveFriendRequest.class.getName(), ChannelNames.REMOVE_FRIEND_REQUEST);
    router.setChannelMapping(RemoveFoeRequest.class.getName(), ChannelNames.REMOVE_FOE_REQUEST);
    router.setChannelMapping(DisconnectedReport.class.getName(), ChannelNames.DISCONNECTED_REPORT);
    // This is sent by the game but currently, the server has no interest in it.
    router.setChannelMapping(BottleneckReport.class.getName(), IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME);
    // This is sent by the game but currently, the server has no interest in it.
    router.setChannelMapping(BottleneckClearedReport.class.getName(), IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME);
    return router;
  }

  /**
   * Filter that lets {@link RequestException RequestExceptions} pass only.
   */
  private GenericSelector<Object> requestExceptionFilter() {
    return (Object source) -> {
      if (!(source instanceof MessagingException)) {
        return false;
      }
      MessagingException messagingException = (MessagingException) source;
      return messagingException.getCause() instanceof RequestException
        || messagingException.getCause() != null && messagingException.getCause().getCause() instanceof RequestException;
    };
  }

  /**
   * Transformer that transforms a {@link MessageHandlingException} into and {@link ErrorResponse}.
   */
  private AbstractTransformer requestExceptionTransformer() {
    return new AbstractTransformer() {
      @Override
      protected Object doTransform(Message<?> message) throws Exception {
        MessagingException messageException = (MessagingException) message.getPayload();
        Message<?> failedMessage = messageException.getFailedMessage();

        // TODO try to make more intuitive? Meanwhile, look at requestExceptionFilter() to understand.
        RequestException requestException;
        if (messageException instanceof MessageTransformationException) {
          requestException = (RequestException) messageException.getCause().getCause();
        } else {
          requestException = (RequestException) messageException.getCause();
        }

        MessageBuilder<ErrorResponse> builder = MessageBuilder
          .withPayload(new ErrorResponse(requestException.getErrorCode(), requestException.getRequestId(), requestException.getArgs()))
          .copyHeaders(message.getHeaders());
        builder.setHeader(CLIENT_CONNECTION, failedMessage.getHeaders().get(CLIENT_CONNECTION, ClientConnection.class));

        return builder.build();
      }
    };
  }
}
