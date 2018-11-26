package com.faforever.server.integration.v2.server;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class V2ServerMessageWrapper {

  @JsonTypeInfo(use = Id.NAME, include = As.EXTERNAL_PROPERTY, property = "type")
  @JsonSubTypes({
    @Type(value = ChatChannelServerMessage.class, name = ChatChannelServerMessage.TYPE_NAME),
    @Type(value = ConnectToPlayerServerMessage.class, name = ConnectToPlayerServerMessage.TYPE_NAME),
    @Type(value = DisconnectPeerServerMessage.class, name = DisconnectPeerServerMessage.TYPE_NAME),
    @Type(value = ErrorServerMessage.class, name = ErrorServerMessage.TYPE_NAME),
    @Type(value = FeaturedModServerMessage.class, name = FeaturedModServerMessage.TYPE_NAME),
    @Type(value = GameInfoServerMessage.class, name = GameInfoServerMessage.TYPE_NAME),
    @Type(value = GameInfosServerMessage.class, name = GameInfosServerMessage.TYPE_NAME),
    @Type(value = HostGameServerMessage.class, name = HostGameServerMessage.TYPE_NAME),
    @Type(value = InfoServerMessage.class, name = InfoServerMessage.TYPE_NAME),
    @Type(value = IceServersServerMessage.class, name = IceServersServerMessage.TYPE_NAME),
    @Type(value = LoginDetailsServerMessage.class, name = LoginDetailsServerMessage.TYPE_NAME),
    @Type(value = MatchAvailableServerMessage.class, name = MatchAvailableServerMessage.TYPE_NAME),
    @Type(value = PlayerServerMessage.class, name = PlayerServerMessage.TYPE_NAME),
    @Type(value = PlayersServerMessage.class, name = PlayersServerMessage.TYPE_NAME),
    @Type(value = SocialRelationsServerMessage.class, name = SocialRelationsServerMessage.TYPE_NAME),
    @Type(value = StartGameProcessServerMessage.class, name = StartGameProcessServerMessage.TYPE_NAME),
    @Type(value = UpdatedAchievementsServerMessage.class, name = UpdatedAchievementsServerMessage.TYPE_NAME),
    @Type(value = MatchCreatedServerMessage.class, name = MatchCreatedServerMessage.TYPE_NAME),
    @Type(value = ConnectToHostServerMessage.class, name = ConnectToHostServerMessage.TYPE_NAME),
    @Type(value = GameResultMessage.class, name = GameResultMessage.TYPE_NAME),
  })
  private V2ServerMessage data;
}
