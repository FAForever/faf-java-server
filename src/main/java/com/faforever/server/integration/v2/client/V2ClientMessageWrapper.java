package com.faforever.server.integration.v2.client;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class V2ClientMessageWrapper {

  @JsonTypeInfo(use = Id.NAME, include = As.EXTERNAL_PROPERTY, property = "type")
  @JsonSubTypes({
    @Type(value = AgreeDrawClientMessage.class, name = "agreeDraw"),
    @Type(value = AiOptionClientMessage.class, name = "aiOption"),
    @Type(value = ArmyOutcomeClientMessage.class, name = "armyOutcomeReport"),
    @Type(value = ArmyScoreClientMessage.class, name = "armyScore"),
    @Type(value = ArmyStatsClientMessage.class, name = "armyStats"),
    @Type(value = BroadcastClientMessage.class, name = "broadcast"),
    @Type(value = ClearSlotClientMessage.class, name = "clearSlot"),
    @Type(value = CoopMissionCompletedClientMessage.class, name = "coopComplete"),
    @Type(value = CancelMatchSearchClientMessage.class, name = "cancelMatchSearch"),
    @Type(value = DisconnectClientClientMessage.class, name = "disconnectClient"),
    @Type(value = DisconnectPeerClientMessage.class, name = "disconnectPeer"),
    @Type(value = GameDesyncClientMessage.class, name = "gameDesync"),
    @Type(value = GameModsClientMessage.class, name = "gameMods"),
    @Type(value = GameOptionClientMessage.class, name = "gameOption"),
    @Type(value = GameStateClientMessage.class, name = "gameState"),
    @Type(value = HostGameClientMessage.class, name = "hostGame"),
    @Type(value = IceClientMessage.class, name = "iceMessage"),
    @Type(value = JoinGameClientMessage.class, name = "joinGame"),
    @Type(value = ListAvatarsClientMessage.class, name = "listAvatars"),
    @Type(value = ListIceServersClientMessage.class, name = "listIceServers"),
    @Type(value = LoginClientMessage.class, name = "login"),
    @Type(value = SearchMatchClientMessage.class, name = "searchMatch"),
    @Type(value = TeamKillClientMessage.class, name = "teamKill"),
    @Type(value = PlayerDefeatedClientMessage.class, name = "playerDefeated"),
    @Type(value = PlayerOptionClientMessage.class, name = "playerOption"),
    @Type(value = RestoreGameSessionClientMessage.class, name = "restoreGameSession"),
    @Type(value = SelectAvatarClientMessage.class, name = "selectAvatar"),
    @Type(value = TeamKillClientMessage.class, name = "teamKill"),
    @Type(value = CreateMatchMessage.class, name = "createMatch"),
  })
  private V2ClientMessage data;
}
