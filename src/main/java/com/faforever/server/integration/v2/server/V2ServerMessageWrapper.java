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
    @Type(value = ChatChannelServerMessage.class, name = "chatChannel"),
    @Type(value = ConnectToPlayerServerMessage.class, name = "connectToPeer"),
    @Type(value = DisconnectPeerServerMessage.class, name = "disconnectFromPeer"),
    @Type(value = ErrorServerMessage.class, name = "error"),
    @Type(value = FeaturedModServerMessage.class, name = "featuredMod"),
    @Type(value = GameInfoServerMessage.class, name = "game"),
    @Type(value = GameInfosServerMessage.class, name = "game"),
    @Type(value = HostGameServerMessage.class, name = "hostGame"),
    @Type(value = InfoServerMessage.class, name = "info"),
    @Type(value = IceServersServerMessage.class, name = "iceServers"),
    @Type(value = LoginDetailsServerMessage.class, name = "loginDetails"),
    @Type(value = MatchAvailableServerMessage.class, name = "matchAvailable"),
    @Type(value = PlayerServerMessage.class, name = "player"),
    @Type(value = PlayersServerMessage.class, name = "players"),
    @Type(value = SocialRelationServerMessage.class, name = "socialRelations"),
    @Type(value = StartGameProcessServerMessage.class, name = "startGameProcess"),
    @Type(value = UpdatedAchievementsServerMessage.class, name = "updatedAchievements"),
    @Type(value = MatchCreatedServerMessage.class, name = "matchCreated"),
    @Type(value = ConnectToHostServerMessage.class, name = "connectToHost"),
  })
  private V2ServerMessage data;
}
