package com.faforever.server.config.integration;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.integration.ClientConnectionChannelInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.security.channel.SecuredChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates Spring Integration channels. Bean names must match their entry in {@link
 * com.faforever.server.integration.ChannelNames ChannelNames}. Channels that aren't explicitly configured here will be
 * implicitly instantiated as {@link DirectChannel} by Spring.
 */
@Configuration
public class ChannelConfiguration {

  private static final String CHANNEL_SECURITY_INTERCEPTOR = "channelSecurityInterceptor";
  private static final String ROLE_USER = "ROLE_USER";
  private static final String ROLE_ADMIN = "ROLE_ADMIN";

  /**
   * Channel that receives {@link com.faforever.server.common.ClientMessage ClientMessages} created by  client adapters.
   * Each messages will have their headers enriched with connection information.
   */
  @Bean(name = ChannelNames.CLIENT_INBOUND)
  public MessageChannel clientInbound(ClientConnectionChannelInterceptor clientConnectionChannelInterceptor) {
    return MessageChannels.direct()
      .interceptor(clientConnectionChannelInterceptor)
      .get();
  }

  /**
   * Takes all messages to be processed by the server (no matter whether produced by internal events or incoming
   * messages) and schedules them to be processed by a single thread. Each messages will have their headers enriched
   * with authentication information.
   *
   * @see ChannelNames#INBOUND_DISPATCH
   */
  @Bean(name = ChannelNames.INBOUND_DISPATCH)
  public MessageChannel inboundDispatch(SecurityContextChannelInterceptor securityContextChannelInterceptor) {
    return MessageChannels
      .executor(Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "inbound-dispatch")))
      .interceptor(securityContextChannelInterceptor)
      .get();
  }

  /**
   * Takes all {@link com.faforever.server.common.ServerMessage ServerMessages} to be received by a single client and
   * schedules them to be sent by a single thread.
   */
  @Bean(name = ChannelNames.CLIENT_OUTBOUND)
  public MessageChannel clientOutbound() {
    return MessageChannels
      .executor(Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "client-outbound")))
      .get();
  }

  /**
   * Executor channel with limited queue size to prevent out of memory on excessive message production.
   *
   * @see ChannelNames#LEGACY_TCP_OUTBOUND
   */
  @Bean(name = ChannelNames.LEGACY_TCP_OUTBOUND)
  public MessageChannel legacyTcpOutbound(ServerProperties properties) {
    AtomicInteger counter = new AtomicInteger();
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
      1,
      4,
      30, TimeUnit.SECONDS,
      new LinkedBlockingQueue<>(properties.getMessaging().getLegacyAdapterOutboundQueueSize()),
      runnable -> new Thread(runnable, "legacy-tcp-out-" + counter.incrementAndGet()));

    return MessageChannels.executor(threadPoolExecutor).get();
  }

  @Bean(name = ChannelNames.JOIN_GAME_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel joinGameRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.LIST_AVATAR)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel avatarRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.LEGACY_ADD_FRIEND_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel addFriendRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.LEGACY_REMOVE_FRIEND_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel removeFriendRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.LEGACY_ADD_FOE_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel addFoeRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.LEGACY_REMOVE_FOE_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel removeFoeRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.HOST_GAME_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel hostGameRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.UPDATE_GAME_STATE_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel updateGameStateRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.GAME_OPTION_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel gameOptionRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.PLAYER_OPTION_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel playerOptionRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.CLEAR_SLOT_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel clearSlotRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.AI_OPTION_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel aiOptionRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.DESYNC_REPORT)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel desyncReport() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.ARMY_SCORE_REPORT)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel armyScoreReport() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.ARMY_OUTCOME_REPORT)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel armyOutcomeReport() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.GAME_MODS_REPORT)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel gameModsReport() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.GAME_MODS_COUNT_REPORT)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel gameModsCountReport() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.OPERATION_COMPLETE_REPORT)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel operationCompleteReport() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.GAME_STATISTICS_REPORT)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel gameStatisticsReport() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.ENFORCE_RATING_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel enforceRatingRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.TEAM_KILL_REPORT)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel teamKillReport() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.LEGACY_COOP_LIST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel listCoopRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.MATCH_MAKER_SEARCH_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel matchMakerSearchRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.MATCH_MAKER_CANCEL_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel matchMakerCancelRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.DISCONNECT_PEER_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel disconnectPeerRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.DISCONNECT_CLIENT_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_ADMIN)
  public SubscribableChannel disconnectClientRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.ICE_SERVERS_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel iceServersRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.ICE_MESSAGE)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel iceMessage() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.RESTORE_GAME_SESSION_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel restoreGameSessionRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.MUTUALLY_AGREED_DRAW_REQUEST)
  @SecuredChannel(interceptor = CHANNEL_SECURITY_INTERCEPTOR, sendAccess = ROLE_USER)
  public SubscribableChannel mutuallyAgreedDrawRequest() {
    return MessageChannels.direct().get();
  }

  @Bean(name = ChannelNames.CLIENT_DISCONNECTED_EVENT)
  public SubscribableChannel clientDisconnectedEvent() {
    return MessageChannels.publishSubscribe().get();
  }
}
