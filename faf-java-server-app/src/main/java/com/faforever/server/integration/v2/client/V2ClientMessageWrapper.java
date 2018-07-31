package com.faforever.server.integration.v2.client;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
class V2ClientMessageWrapper {

  @JsonTypeInfo(use = Id.NAME, include = As.EXTERNAL_PROPERTY, property = "type")
  @JsonSubTypes({
    @Type(value = PingClientMessage.class, name = PingClientMessage.TYPE_NAME),
    @Type(value = AgreeDrawClientMessage.class, name = AgreeDrawClientMessage.TYPE_NAME),
    @Type(value = AiOptionClientMessage.class, name = AiOptionClientMessage.TYPE_NAME),
    @Type(value = ArmyOutcomeClientMessage.class, name = ArmyOutcomeClientMessage.TYPE_NAME),
    @Type(value = ArmyScoreClientMessage.class, name = ArmyScoreClientMessage.TYPE_NAME),
    @Type(value = ArmyStatsClientMessage.class, name = ArmyStatsClientMessage.TYPE_NAME),
    @Type(value = BroadcastClientMessage.class, name = BroadcastClientMessage.TYPE_NAME),
    @Type(value = ClearSlotClientMessage.class, name = ClearSlotClientMessage.TYPE_NAME),
    @Type(value = CoopMissionCompletedClientMessage.class, name = CoopMissionCompletedClientMessage.TYPE_NAME),
    @Type(value = CancelMatchSearchClientMessage.class, name = CancelMatchSearchClientMessage.TYPE_NAME),
    @Type(value = DisconnectClientClientMessage.class, name = DisconnectClientClientMessage.TYPE_NAME),
    @Type(value = DisconnectPeerClientMessage.class, name = DisconnectPeerClientMessage.TYPE_NAME),
    @Type(value = GameDesyncClientMessage.class, name = GameDesyncClientMessage.TYPE_NAME),
    @Type(value = GameModsClientMessage.class, name = GameModsClientMessage.TYPE_NAME),
    @Type(value = GameOptionClientMessage.class, name = GameOptionClientMessage.TYPE_NAME),
    @Type(value = GameStateClientMessage.class, name = GameStateClientMessage.TYPE_NAME),
    @Type(value = HostGameClientMessage.class, name = HostGameClientMessage.TYPE_NAME),
    @Type(value = IceClientMessage.class, name = IceClientMessage.TYPE_NAME),
    @Type(value = JoinGameClientMessage.class, name = JoinGameClientMessage.TYPE_NAME),
    @Type(value = ListAvatarsClientMessage.class, name = ListAvatarsClientMessage.TYPE_NAME),
    @Type(value = ListIceServersClientMessage.class, name = ListIceServersClientMessage.TYPE_NAME),
    @Type(value = LoginClientMessage.class, name = LoginClientMessage.TYPE_NAME),
    @Type(value = SearchMatchClientMessage.class, name = SearchMatchClientMessage.TYPE_NAME),
    @Type(value = TeamKillClientMessage.class, name = TeamKillClientMessage.TYPE_NAME),
    @Type(value = PlayerDefeatedClientMessage.class, name = PlayerDefeatedClientMessage.TYPE_NAME),
    @Type(value = PlayerOptionClientMessage.class, name = PlayerOptionClientMessage.TYPE_NAME),
    @Type(value = RestoreGameSessionClientMessage.class, name = RestoreGameSessionClientMessage.TYPE_NAME),
    @Type(value = SelectAvatarClientMessage.class, name = SelectAvatarClientMessage.TYPE_NAME),
    @Type(value = CreateMatchClientMessage.class, name = CreateMatchClientMessage.TYPE_NAME),
    @Type(value = VerifyPlayerClientMessage.class, name = VerifyPlayerClientMessage.TYPE_NAME),
    @Type(value = GameChatMessageClientMessage.class, name = GameChatMessageClientMessage.TYPE_NAME)
  })
  private V2ClientMessage data;
}
