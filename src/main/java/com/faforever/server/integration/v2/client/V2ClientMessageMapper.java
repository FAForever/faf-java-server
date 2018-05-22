package com.faforever.server.integration.v2.client;

import com.faforever.server.avatar.ListAvatarsRequest;
import com.faforever.server.avatar.SelectAvatarRequest;
import com.faforever.server.client.BroadcastRequest;
import com.faforever.server.client.DisconnectClientRequest;
import com.faforever.server.client.LoginRequest;
import com.faforever.server.client.PingReport;
import com.faforever.server.coop.CoopMissionCompletedReport;
import com.faforever.server.game.AiOptionReport;
import com.faforever.server.game.ArmyOutcomeReport;
import com.faforever.server.game.ArmyScoreReport;
import com.faforever.server.game.ClearSlotRequest;
import com.faforever.server.game.DesyncReport;
import com.faforever.server.game.DisconnectPeerRequest;
import com.faforever.server.game.GameChatMessageReport;
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
import com.faforever.server.integration.legacy.transformer.RestoreGameSessionRequest;
import com.faforever.server.matchmaker.CreateMatchRequest;
import com.faforever.server.matchmaker.MatchMakerCancelRequest;
import com.faforever.server.matchmaker.MatchMakerSearchRequest;
import com.faforever.server.stats.ArmyStatisticsReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ArmyStatsMapper.class)
public interface V2ClientMessageMapper {

  AiOptionReport map(AiOptionClientMessage message);

  ArmyScoreReport map(ArmyScoreClientMessage message);

  BroadcastRequest map(BroadcastClientMessage message);

  @Mapping(source = "pool", target = "poolName")
  MatchMakerCancelRequest map(CancelMatchSearchClientMessage message);

  @Mapping(expression = "java(java.time.Duration.ofSeconds(message.getTime()))", target = "time")
  CoopMissionCompletedReport map(CoopMissionCompletedClientMessage message);

  DisconnectClientRequest map(DisconnectClientClientMessage message);

  DisconnectPeerRequest map(DisconnectPeerClientMessage message);

  GameOptionReport map(GameOptionClientMessage message);

  GameStateReport map(GameStateClientMessage message);

  IceMessage map(IceClientMessage message);

  JoinGameRequest map(JoinGameClientMessage message);

  PlayerOptionReport map(PlayerOptionClientMessage message);

  RestoreGameSessionRequest map(RestoreGameSessionClientMessage message);

  @Mapping(source = "stats", target = "armyStatistics")
  ArmyStatisticsReport map(ArmyStatsClientMessage message);

  @Mapping(source = "pool", target = "poolName")
  MatchMakerSearchRequest map(SearchMatchClientMessage message);

  @Mapping(target = "avatarUrl", ignore = true)
  SelectAvatarRequest map(SelectAvatarClientMessage message);

  @Mapping(expression = "java(java.time.Duration.ofSeconds(message.getTime()))", target = "time")
  TeamKillReport map(TeamKillClientMessage message);

  @Mapping(source = "modName", target = "mod")
  HostGameRequest map(HostGameClientMessage message);

  @Mapping(source = "uids", target = "modUids")
  GameModsReport map(GameModsClientMessage message);

  @Mapping(source = "map", target = "mapVersionId")
  CreateMatchRequest map(CreateMatchClientMessage message);

  LoginRequest map(LoginClientMessage message);

  default PlayerDefeatedReport map(@SuppressWarnings("unused") PlayerDefeatedClientMessage message) {
    return PlayerDefeatedReport.INSTANCE;
  }

  default ListAvatarsRequest map(@SuppressWarnings("unused") ListAvatarsClientMessage message) {
    return ListAvatarsRequest.INSTANCE;
  }

  default IceServersRequest map(@SuppressWarnings("unused") ListIceServersClientMessage message) {
    return IceServersRequest.INSTANCE;
  }

  default PingReport map(@SuppressWarnings("unused") PingClientMessage message) {
    return PingReport.INSTANCE;
  }

  default DesyncReport map(@SuppressWarnings("unused") GameDesyncClientMessage message) {
    return DesyncReport.INSTANCE;
  }

  default ClearSlotRequest map(ClearSlotClientMessage message) {
    return ClearSlotRequest.valueOf(message.getSlotId());
  }

  default ArmyOutcomeReport map(ArmyOutcomeClientMessage message) {
    return new ArmyOutcomeReport(message.getArmyId(), message.getOutcome(), message.getScore());
  }

  default MutuallyAgreedDrawRequest map(@SuppressWarnings("unused") AgreeDrawClientMessage message) {
    return MutuallyAgreedDrawRequest.INSTANCE;
  }

  default GameChatMessageReport map(GameChatMessageClientMessage message) {
    return new GameChatMessageReport(message.getMessage());
  }
}
