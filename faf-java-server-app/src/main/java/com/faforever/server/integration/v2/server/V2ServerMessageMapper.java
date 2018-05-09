package com.faforever.server.integration.v2.server;

import com.faforever.server.chat.JoinChatChannelResponse;
import com.faforever.server.client.AvatarsResponse;
import com.faforever.server.client.ConnectToHostResponse;
import com.faforever.server.client.ConnectToPeerResponse;
import com.faforever.server.client.DisconnectPlayerFromGameResponse;
import com.faforever.server.client.GameResponses;
import com.faforever.server.client.IceServersResponse;
import com.faforever.server.client.InfoResponse;
import com.faforever.server.client.PlayerResponses;
import com.faforever.server.client.UpdatedAchievementsResponse;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.ErrorResponse;
import com.faforever.server.game.GameResponse;
import com.faforever.server.game.HostGameResponse;
import com.faforever.server.game.StartGameProcessResponse;
import com.faforever.server.matchmaker.MatchCreatedResponse;
import com.faforever.server.matchmaker.MatchMakerResponse;
import com.faforever.server.mod.FeaturedModResponse;
import com.faforever.server.player.LoginDetailsResponse;
import com.faforever.server.player.PlayerResponse;
import com.faforever.server.social.SocialRelationListResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface V2ServerMessageMapper {
  ChatChannelServerMessage map(JoinChatChannelResponse source);

  ConnectToPlayerServerMessage map(ConnectToPeerResponse source);

  DisconnectPeerServerMessage map(DisconnectPlayerFromGameResponse source);

  FeaturedModServerMessage map(FeaturedModResponse source);

  @Mapping(source = "featuredModTechnicalName", target = "mod")
  @Mapping(source = "technicalMapName", target = "map")
  @Mapping(source = "featuredModVersion", target = "modVersion")
  @Mapping(source = "featuredModFileVersions", target = "modFileVersions")
  GameInfoServerMessage map(GameResponse source);

  IceServersServerMessage map(IceServersResponse source);

  InfoServerMessage map(InfoResponse source);

  LoginDetailsServerMessage map(LoginDetailsResponse source);

  @Mapping(source = "poolName", target = "pool")
  MatchAvailableServerMessage map(MatchMakerResponse source);

  SocialRelationsServerMessage map(SocialRelationListResponse source);

  @Mapping(source = "mapFolderName", target = "map")
  StartGameProcessServerMessage map(StartGameProcessResponse source);

  ConnectToHostServerMessage map(ConnectToHostResponse source);

  UpdatedAchievementsServerMessage map(UpdatedAchievementsResponse source);

  @Mapping(source = "responses", target = "players")
  PlayersServerMessage map(PlayerResponses source);

  PlayerServerMessage map(PlayerResponse source);

  @Mapping(source = "mapFilename", target = "mapName")
    // TODO check and document if the map files are folder names, file names or file names with path
  HostGameServerMessage map(HostGameResponse source);

  @Mapping(source = "responses", target = "games")
  GameInfosServerMessage map(GameResponses source);

  MatchCreatedServerMessage map(MatchCreatedResponse source);

  AvatarsServerMessage map(AvatarsResponse source);

  default ErrorServerMessage map(ErrorResponse source) {
    ErrorCode errorCode = source.getErrorCode();
    Object[] args = source.getArgs();

    return new ErrorServerMessage(
      errorCode.getCode(),
      MessageFormat.format(errorCode.getTitle(), args),
      MessageFormat.format(errorCode.getDetail(), args),
      Optional.ofNullable(source.getRequestId()).map(UUID::toString).orElse(null),
      args
    );
  }
}
