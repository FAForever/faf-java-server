package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.avatar.AddAvatarAdminRequest;
import com.faforever.server.avatar.GetAvatarsAdminRequest;
import com.faforever.server.avatar.ListAvatarsRequest;
import com.faforever.server.avatar.RemoveAvatarAdminRequest;
import com.faforever.server.avatar.SelectAvatarRequest;
import com.faforever.server.client.BroadcastRequest;
import com.faforever.server.client.DisconnectClientRequest;
import com.faforever.server.client.LegacyLoginRequest;
import com.faforever.server.client.LegacySessionRequest;
import com.faforever.server.client.PingReport;
import com.faforever.server.common.ClientMessage;
import com.faforever.server.coop.CoopMissionCompletedReport;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.ProgrammingError;
import com.faforever.server.error.RequestException;
import com.faforever.server.error.Requests;
import com.faforever.server.game.AiOptionReport;
import com.faforever.server.game.ArmyOutcomeReport;
import com.faforever.server.game.ArmyScoreReport;
import com.faforever.server.game.BottleneckClearedReport;
import com.faforever.server.game.BottleneckReport;
import com.faforever.server.game.ClearSlotRequest;
import com.faforever.server.game.DesyncReport;
import com.faforever.server.game.DisconnectPeerRequest;
import com.faforever.server.game.Faction;
import com.faforever.server.game.GameChatMessageReport;
import com.faforever.server.game.GameEndedReport;
import com.faforever.server.game.GameModsCountReport;
import com.faforever.server.game.GameModsReport;
import com.faforever.server.game.GameOptionReport;
import com.faforever.server.game.GameStateReport;
import com.faforever.server.game.GameVisibility;
import com.faforever.server.game.HostGameRequest;
import com.faforever.server.game.JoinGameRequest;
import com.faforever.server.game.MutuallyAgreedDrawRequest;
import com.faforever.server.game.Outcome;
import com.faforever.server.game.PlayerDefeatedReport;
import com.faforever.server.game.PlayerDisconnectedReport;
import com.faforever.server.game.PlayerGameState;
import com.faforever.server.game.PlayerOptionReport;
import com.faforever.server.game.TeamKillReport;
import com.faforever.server.game.VerifyPlayerReport;
import com.faforever.server.ice.IceMessage;
import com.faforever.server.ice.IceServersRequest;
import com.faforever.server.integration.legacy.LegacyClientMessageType;
import com.faforever.server.matchmaker.MatchMakerCancelRequest;
import com.faforever.server.matchmaker.MatchMakerSearchRequest;
import com.faforever.server.social.AddFoeRequest;
import com.faforever.server.social.AddFriendRequest;
import com.faforever.server.social.RemoveFoeRequest;
import com.faforever.server.social.RemoveFriendRequest;
import com.faforever.server.stats.ArmyStatistics;
import com.faforever.server.stats.ArmyStatisticsReport;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.integration.transformer.GenericTransformer;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.nocatch.NoCatch.noCatch;

/**
 * Transforms requests of the legacy protocol to internal client message objects.
 */
@Slf4j
public class LegacyRequestTransformer implements GenericTransformer<Map<String, Object>, ClientMessage> {

  private final ObjectMapper objectMapper;

  public LegacyRequestTransformer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public ClientMessage transform(Map<String, Object> source) {
    String command = (String) source.get("command");
    LegacyClientMessageType messageType = LegacyClientMessageType.fromString(command);
    Requests.verify(messageType != null, ErrorCode.INVALID_COMMAND, command);
    switch (messageType) {
      case HOST_GAME:
        return handleHostGame(source);
      case JOIN_GAME:
        return new JoinGameRequest(((Number) source.get("uid")).intValue(), (String) source.get("password"));
      case ASK_SESSION:
        String userAgent = (String) source.get("user_agent");
        return LegacySessionRequest.forUserAgent(userAgent);
      case SOCIAL_ADD:
        return handleSocialAdd(source);
      case SOCIAL_REMOVE:
        return handleSocialRemove(source);
      case LOGIN:
        return handleLogin(source);
      case GAME_MATCH_MAKING:
        return handleMatchMaking(source);
      case AVATAR:
        return handleAvatar(source);
      case PING:
        return PingReport.INSTANCE;
      case GAME_STATE:
        return handleGameState(source);
      case GAME_OPTION:
        List<Object> args = getArgs(source);
        return new GameOptionReport((String) args.get(0), args.get(1));
      case GAME_ENDED:
        return GameEndedReport.INSTANCE;
      case PLAYER_OPTION:
        args = getArgs(source);
        return new PlayerOptionReport(Integer.parseInt((String) args.get(0)), (String) args.get(1), args.get(2));
      case VERIFY_PLAYER:
        args = getArgs(source);
        return new VerifyPlayerReport(
          (int) args.get(0),
          (String) args.get(1),
          (float) args.get(2),
          (float) args.get(3),
          (String) args.get(4),
          (String) args.get(5),
          (String) args.get(6)
        );
      case CLEAR_SLOT:
        args = getArgs(source);
        return ClearSlotRequest.valueOf((int) args.get(0));
      case DESYNC:
        return DesyncReport.INSTANCE;
      case GAME_MODS:
        return handleGameMods(source);
      case GAME_RESULT:
        return handleGameResult(source);
      case OPERATION_COMPLETE:
        return handleOperationComplete(source);
      case JSON_STATS:
        return handleJsonStats(source);
      case ENFORCE_RATING:
        return PlayerDefeatedReport.INSTANCE;
      case TEAMKILL_REPORT:
        return handleTeamKillReport(source);
      case MUTUAL_DRAW:
        return MutuallyAgreedDrawRequest.INSTANCE;
      case AI_OPTION:
        return handleAiOption(source);
      case INITIATE_TEST:
        throw new RequestException(ErrorCode.UNSUPPORTED_REQUEST, source);
      case ICE_SERVERS:
        return IceServersRequest.INSTANCE;
      case ICE_MESSAGE:
        args = getArgs(source);
        return new IceMessage((int) args.get(0), args.get(1));
      case RESTORE_GAME_SESSION:
        return new RestoreGameSessionRequest((int) source.get("game_id"));
      case CREATE_ACCOUNT:
        throw new RequestException(ErrorCode.CREATE_ACCOUNT_IS_DEPRECATED);
      case ADMIN:
        return handleAdminAction(source);
      case DISCONNECTED:
        args = getArgs(source);
        return new PlayerDisconnectedReport((int) args.get(0));
      case BOTTLENECK:
        return BottleneckReport.INSTANCE;
      case CHAT:
        return handleGameChatMessage((String) getArgs(source).get(0));
      case BOTTLENECK_CLEARED:
        return BottleneckClearedReport.INSTANCE;
      default:
        throw new ProgrammingError("Uncovered message type: " + messageType);
    }
  }

  private ClientMessage handleGameChatMessage(String message) {
    return new GameChatMessageReport(message);
  }

  @NotNull
  private ClientMessage handleGameState(Map<String, Object> source) {
    String string = (String) getArgs(source).get(0);
    // The legacy client sends "Ended" when the process closes. The game never sends this, yet.
    if (PlayerGameState.ENDED.getString().equals(string)) {
      string = PlayerGameState.CLOSED.getString();
    }
    return new GameStateReport(PlayerGameState.fromString(string));
  }

  private ClientMessage handleAvatar(Map<String, Object> source) {
    switch ((String) source.get("action")) {
      case "list_avatar":
        return ListAvatarsRequest.INSTANCE;
      case "select":
        return new SelectAvatarRequest(null, (String) source.get("avatar"));
      default:
        throw new RequestException(ErrorCode.UNSUPPORTED_REQUEST, source);
    }
  }

  private ClientMessage handleAdminAction(Map<String, Object> source) {
    switch ((String) source.get("action")) {
      case "closeFA":
        return new DisconnectPeerRequest((int) source.get("user_id"));
      case "closeLobby":
        return new DisconnectClientRequest(((Number) source.get("user_id")).intValue());
      case "requestavatars":
        return new GetAvatarsAdminRequest();
      case "remove_avatar":
        return new RemoveAvatarAdminRequest(((Number) source.get("idavatar")).intValue(), ((Number) source.get("iduser")).intValue());
      case "add_avatar":
        return new AddAvatarAdminRequest(((Number) source.get("idavatar")).intValue(), ((Number) source.get("iduser")).intValue());
      case "broadcast":
        return new BroadcastRequest((String) source.get("message"));
      default:
        throw new RequestException(ErrorCode.UNSUPPORTED_REQUEST, source);
    }
  }

  private ClientMessage handleAiOption(Map<String, Object> source) {
    List<Object> args;
    args = getArgs(source);
    return new AiOptionReport((String) args.get(0), (String) args.get(1), args.get(2));
  }

  private ClientMessage handleTeamKillReport(Map<String, Object> source) {
    List<Object> args;
    args = getArgs(source);
    return new TeamKillReport(
      Duration.ofSeconds((int) args.get(0)),
      (int) args.get(0),
      (String) args.get(1),
      (int) args.get(2),
      (String) args.get(3)
    );
  }

  private ClientMessage handleJsonStats(Map<String, Object> source) {
    return noCatch(() -> {
      JsonNode node = objectMapper.readTree((String) getArgs(source).get(0));
      JsonNode stats = node.get("stats");
      TypeReference<List<ArmyStatistics>> typeReference = new TypeReference<>() {
      };
      JsonParser jsonParser = stats.traverse();
      jsonParser.setCodec(objectMapper);
      return new ArmyStatisticsReport(jsonParser.readValueAs(typeReference));
    });
  }

  private ClientMessage handleOperationComplete(Map<String, Object> source) {
    List<Object> args;
    args = getArgs(source);
    return new CoopMissionCompletedReport(
      (int) args.get(0) == 1,
      (int) args.get(1) == 1,
      Duration.ofSeconds((int) args.get(2))
    );
  }

  private ClientMessage handleGameResult(Map<String, Object> source) {
    List<Object> args;
    args = getArgs(source);
    int armyId = (int) args.get(0);
    String[] results = ((String) args.get(1)).split(" ");
    int score = Integer.parseInt(results[1]);

    if ("score".equals(results[0])) {
      return new ArmyScoreReport(armyId, score);
    }

    Outcome outcome = Outcome.fromString(results[0]);
    return new ArmyOutcomeReport(armyId, outcome, score);
  }

  private ClientMessage handleGameMods(Map<String, Object> source) {
    List<Object> args;
    args = getArgs(source);
    switch ((String) args.get(0)) {
      case "activated":
        return new GameModsCountReport(((Number) args.get(1)).intValue());
      case "uids":
        return new GameModsReport(Arrays.asList(((String) args.get(1)).split(" ")));
      default:
        throw new IllegalArgumentException("Unknown GameMods argument: " + args.get(0));
    }
  }

  private ClientMessage handleMatchMaking(Map<String, Object> source) {
    switch ((String) source.get("state")) {
      case "stop":
        return new MatchMakerCancelRequest("ladder1v1");
      default:
        Object untypedFaction = source.get("faction");
        Faction faction;
        if (untypedFaction instanceof Number) {
          faction = Faction.fromFaValue(((Number) untypedFaction).intValue());
        } else {
          faction = Faction.fromString((String) untypedFaction);
        }
        return new MatchMakerSearchRequest(faction, "ladder1v1");
    }
  }

  private ClientMessage handleLogin(Map<String, Object> source) {
    return new LegacyLoginRequest(
      (String) source.get("login"),
      (String) source.get("password"),
      (String) source.get("unique_id"));
  }

  private ClientMessage handleSocialRemove(Map<String, Object> source) {
    if (source.containsKey("friend")) {
      return new RemoveFriendRequest(((Number) source.get("friend")).intValue());
    } else if (source.containsKey("foe")) {
      return new RemoveFoeRequest(((Number) source.get("foe")).intValue());
    }
    throw new RequestException(ErrorCode.UNSUPPORTED_REQUEST, source);
  }

  private ClientMessage handleSocialAdd(Map<String, Object> source) {
    if (source.containsKey("friend")) {
      return new AddFriendRequest(((Number) source.get("friend")).intValue());
    } else if (source.containsKey("foe")) {
      return new AddFoeRequest(((Number) source.get("foe")).intValue());
    }
    throw new RequestException(ErrorCode.UNSUPPORTED_REQUEST, source);
  }

  private ClientMessage handleHostGame(Map<String, Object> source) {
    return new HostGameRequest(
      (String) source.get("mapname"),
      (String) source.get("title"),
      (String) source.get("mod"),
      (String) source.get("password"),
      GameVisibility.fromString((String) source.get("visibility")),
      source.get("minRating") == null ? null : ((Number) source.get("minRating")).intValue(),
      source.get("maxRating") == null ? null : ((Number) source.get("maxRating")).intValue()
    );
  }

  @SuppressWarnings("unchecked")
  private List<Object> getArgs(Map<String, Object> source) {
    return (List<Object>) source.get("args");
  }
}
