package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.chat.JoinChatChannelResponse;
import com.faforever.server.client.ConnectToHostResponse;
import com.faforever.server.client.ConnectToPeerResponse;
import com.faforever.server.client.DisconnectPlayerFromGameResponse;
import com.faforever.server.client.GameResponses;
import com.faforever.server.client.IceServersResponse;
import com.faforever.server.client.InfoResponse;
import com.faforever.server.client.PlayerResponses;
import com.faforever.server.client.SessionResponse;
import com.faforever.server.client.UpdatedAchievementsResponse;
import com.faforever.server.common.ServerMessage;
import com.faforever.server.coop.CoopMissionResponse;
import com.faforever.server.error.ErrorResponse;
import com.faforever.server.game.GameResultResponse;
import com.faforever.server.game.HostGameResponse;
import com.faforever.server.game.StartGameProcessResponse;
import com.faforever.server.ice.ForwardedIceMessage;
import com.faforever.server.mod.FeaturedModResponse;
import com.faforever.server.player.LoginDetailsResponse;
import com.faforever.server.social.SocialRelationListResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

/**
 * Transforms responses into legacy response formats.
 */
public final class LegacyResponseTransformer implements GenericTransformer<ServerMessage, Map<String, Serializable>> {

  // Can't convert to enum singleton since otherwise spring integration throws some "ambiguous parameter type" exception
  public static final LegacyResponseTransformer INSTANCE = new LegacyResponseTransformer();

  // Welcome to the generics hell
  private final Map<Class<? extends ServerMessage>, GenericTransformer<? extends ServerMessage, Map<String, Serializable>>> transformers;

  private LegacyResponseTransformer() {
    transformers = ImmutableMap.<Class<? extends ServerMessage>, GenericTransformer<? extends ServerMessage, Map<String, Serializable>>>builder()
      .put(StartGameProcessResponse.class, LaunchGameResponseTransformer.INSTANCE)
      .put(SessionResponse.class, SessionResponseTransformer.INSTANCE)
      .put(PlayerResponses.class, PlayerResponsesTransformer.INSTANCE)
      .put(LoginDetailsResponse.class, LoginDetailsResponseTransformer.INSTANCE)
      .put(ErrorResponse.class, ErrorResponseTransformer.INSTANCE)
      .put(InfoResponse.class, InfoResponseTransformer.INSTANCE)
      .put(HostGameResponse.class, HostGameResponseTransformer.INSTANCE)
      .put(ConnectToPeerResponse.class, ConnectToPeerResponseTransformer.INSTANCE)
      .put(FeaturedModResponse.class, FeaturedModResponseTransformer.INSTANCE)
      .put(GameResponses.class, GameResponsesTransformer.INSTANCE)
      .put(CoopMissionResponse.class, CoopMissionsResponseTransformer.INSTANCE)
      .put(DisconnectPlayerFromGameResponse.class, DisconnectPeerResponseTransformer.INSTANCE)
      .put(UpdatedAchievementsResponse.class, UpdatedAchievementsResponseTransformer.INSTANCE)
      .put(JoinChatChannelResponse.class, JoinChatChannelsResponseTransformer.INSTANCE)
      .put(SocialRelationListResponse.class, SocialRelationListResponseTransformer.INSTANCE)
      .put(IceServersResponse.class, IceServersResponseTransformer.INSTANCE)
      .put(ForwardedIceMessage.class, ForwardedIceMessageTransformer.INSTANCE)
      .put(ConnectToHostResponse.class, ConnectToHostResponseTransformer.INSTANCE)
      .put(GameResultResponse.class, GameResultMessageTransformer.INSTANCE)
      .build();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Serializable> transform(ServerMessage source) {
    return getTransformerFor(source.getClass()).transform(source);
  }

  @SuppressWarnings("unchecked")
  private GenericTransformer<ServerMessage, Map<String, Serializable>> getTransformerFor(Class<? extends ServerMessage> source) {
    return (GenericTransformer<ServerMessage, Map<String, Serializable>>) transformers.get(source);
  }
}
