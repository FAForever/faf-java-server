package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.chat.JoinChatChannelResponse;
import com.faforever.server.client.ConnectToPlayerResponse;
import com.faforever.server.client.DisconnectPlayerFromGameResponse;
import com.faforever.server.client.IceServersResponse;
import com.faforever.server.client.InfoResponse;
import com.faforever.server.client.SessionResponse;
import com.faforever.server.client.UpdatedAchievementsResponse;
import com.faforever.server.common.ServerMessage;
import com.faforever.server.coop.CoopMissionResponse;
import com.faforever.server.error.ErrorResponse;
import com.faforever.server.game.GameResponse;
import com.faforever.server.game.HostGameResponse;
import com.faforever.server.integration.response.StartGameProcessResponse;
import com.faforever.server.mod.FeaturedModResponse;
import com.faforever.server.player.UserDetailsResponse;
import com.faforever.server.social.SocialRelationListResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

/**
 * Transforms responses into legacy response formats.
 */
public class LegacyResponseTransformer implements GenericTransformer<ServerMessage, Map<String, Serializable>> {

  // Welcome to the generics hell
  private final Map<Class<? extends ServerMessage>, GenericTransformer<? extends ServerMessage, Map<String, Serializable>>> transformers;

  public LegacyResponseTransformer() {
    transformers = ImmutableMap.<Class<? extends ServerMessage>, GenericTransformer<? extends ServerMessage, Map<String, Serializable>>>builder()
      .put(StartGameProcessResponse.class, LaunchGameResponseTransformer.INSTANCE)
      .put(SessionResponse.class, SessionResponseTransformer.INSTANCE)
      .put(UserDetailsResponse.class, UserDetailsResponseTransformer.INSTANCE)
      .put(ErrorResponse.class, ErrorResponseTransformer.INSTANCE)
      .put(InfoResponse.class, InfoResponseTransformer.INSTANCE)
      .put(HostGameResponse.class, HostGameResponseTransformer.INSTANCE)
      .put(ConnectToPlayerResponse.class, ConnectToPlayerResponseTransformer.INSTANCE)
      .put(FeaturedModResponse.class, FeaturedModResponseTransformer.INSTANCE)
      .put(GameResponse.class, GameResponseTransformer.INSTANCE)
      .put(CoopMissionResponse.class, CoopMissionsResponseTransformer.INSTANCE)
      .put(DisconnectPlayerFromGameResponse.class, DisconnectPeerResponseTransformer.INSTANCE)
      .put(UpdatedAchievementsResponse.class, UpdatedAchievementsTransformer.INSTANCE)
      .put(JoinChatChannelResponse.class, JoinChatChannelsResponseTransformer.INSTANCE)
      .put(SocialRelationListResponse.class, SocialRelationListResponseTransformer.INSTANCE)
      .put(IceServersResponse.class, IceServersResponseTransformer.INSTANCE)
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
