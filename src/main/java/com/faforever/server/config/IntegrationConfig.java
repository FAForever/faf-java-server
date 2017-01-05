package com.faforever.server.config;

import com.faforever.server.integration.ChannelNames;
import com.faforever.server.request.AskSessionRequest;
import com.faforever.server.request.AvatarRequest;
import com.faforever.server.request.HostGameRequest;
import com.faforever.server.request.JoinGameRequest;
import com.faforever.server.request.LoginRequest;
import com.faforever.server.request.SocialAddRequest;
import com.faforever.server.request.SocialRemoveRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.integration.router.PayloadTypeRouter;
import org.springframework.messaging.Message;

import java.util.Collections;
import java.util.List;

@Configuration
@IntegrationComponentScan
public class IntegrationConfig {

  /**
   * Reads messages from the standard client inbound channel.
   */
  @Bean
  public IntegrationFlow inboundFlow() {
    return IntegrationFlows
      .from(ChannelNames.CLIENT_INBOUND)
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

  private AbstractMessageRouter outboundRouter() {
    return new AbstractMappingMessageRouter() {
      @Override
      protected List<Object> getChannelKeys(Message<?> message) {
        if (message.getHeaders().get("legacy", Boolean.class)) {
          return Collections.singletonList(ChannelNames.LEGACY_OUTBOUND);
        }
        throw new UnsupportedOperationException("Only legacy outbound route is specified");
      }
    };
  }

  private PayloadTypeRouter inboundRouter() {
    PayloadTypeRouter payloadTypeRouter = new PayloadTypeRouter();
    payloadTypeRouter.setChannelMapping(HostGameRequest.class.getName(), ChannelNames.HOST_GAME_REQUEST);
    payloadTypeRouter.setChannelMapping(JoinGameRequest.class.getName(), ChannelNames.JOIN_GAME);
    payloadTypeRouter.setChannelMapping(LoginRequest.class.getName(), ChannelNames.LEGACY_LOGIN_REQUEST);
    payloadTypeRouter.setChannelMapping(AskSessionRequest.class.getName(), ChannelNames.LEGACY_SESSION_REQUEST);
    payloadTypeRouter.setChannelMapping(AvatarRequest.class.getName(), ChannelNames.LEGACY_AVATAR_REQUEST);
    payloadTypeRouter.setChannelMapping(SocialAddRequest.class.getName(), ChannelNames.LEGACY_ADD_FRIEND_REQUEST);
    payloadTypeRouter.setChannelMapping(SocialRemoveRequest.class.getName(), ChannelNames.LEGACY_REMOVE_FRIEND_REQUEST);

    return payloadTypeRouter;
  }
}
