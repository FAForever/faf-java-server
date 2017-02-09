package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.avatar.AddAvatarAdminRequest;
import com.faforever.server.avatar.AvatarMessage;
import com.faforever.server.avatar.GetAvatarsAdminRequest;
import com.faforever.server.avatar.RemoveAvatarAdminRequest;
import com.faforever.server.client.BroadcastRequest;
import com.faforever.server.client.DisconnectClientRequest;
import com.faforever.server.client.LoginMessage;
import com.faforever.server.client.SessionRequest;
import com.faforever.server.coop.CoopMissionCompletedReport;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.ProgrammingError;
import com.faforever.server.error.Requests;
import com.faforever.server.game.AiOptionReport;
import com.faforever.server.game.ArmyOutcomeReport;
import com.faforever.server.game.ArmyScoreReport;
import com.faforever.server.game.ClearSlotRequest;
import com.faforever.server.game.DesyncReport;
import com.faforever.server.game.DisconnectPeerRequest;
import com.faforever.server.game.EnforceRatingRequest;
import com.faforever.server.game.Faction;
import com.faforever.server.game.GameAccess;
import com.faforever.server.game.GameModsCountReport;
import com.faforever.server.game.GameModsReport;
import com.faforever.server.game.GameOptionReport;
import com.faforever.server.game.GameVisibility;
import com.faforever.server.game.JoinGameRequest;
import com.faforever.server.game.Outcome;
import com.faforever.server.game.PlayerGameState;
import com.faforever.server.game.PlayerOptionReport;
import com.faforever.server.game.TeamKillReport;
import com.faforever.server.integration.legacy.LegacyClientMessageType;
import com.faforever.server.integration.request.GameStateReport;
import com.faforever.server.integration.request.HostGameRequest;
import com.faforever.server.matchmaker.MatchMakerCancelRequest;
import com.faforever.server.matchmaker.MatchMakerSearchRequest;
import com.faforever.server.request.ClientMessage;
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
    LegacyClientMessageType messageType = LegacyClientMessageType.fromString((String) source.get("command"));
    switch (messageType) {
      case HOST_GAME:
        return handleHostGame(source);
      case JOIN_GAME:
        return new JoinGameRequest(((Double) source.get("uid")).intValue(), (String) source.get("password"));
      case ASK_SESSION:
        return new SessionRequest();
      case SOCIAL_ADD:
        return handleSocialAdd(source);
      case SOCIAL_REMOVE:
        return handleSocialRemove(source);
      case LOGIN:
        return handleLogin(source);
      case GAME_MATCH_MAKING:
        return handleMatchMaking(source);
      case AVATAR:
        return handleAvatar();
      case GAME_STATE:
        return new GameStateReport(PlayerGameState.fromString((String) getArgs(source).get(0)));
      case GAME_OPTION:
        List<Object> args = getArgs(source);
        return new GameOptionReport((String) args.get(0), args.get(1));
      case PLAYER_OPTION:
        args = getArgs(source);
        return new PlayerOptionReport(Integer.parseInt((String) args.get(0)), (String) args.get(1), args.get(2));
      case CLEAR_SLOT:
        args = getArgs(source);
        return new ClearSlotRequest((int) args.get(0));
      case DESYNC:
        return new DesyncReport();
      case GAME_MODS:
        return handleGameMods(source);
      case GAME_RESULT:
        return handleGameResult(source);
      case OPERATION_COMPLETE:
        return handleOperationComplete(source);
      case JSON_STATS:
        return handleJsonStats(source);
      case ENFORCE_RATING:
        return new EnforceRatingRequest();
      case TEAMKILL_REPORT:
        return handleTeamKillReport(source);
      case AI_OPTION:
        return handleAiOption(source);
      case INITIATE_TEST:
        log.warn("Ignoring " + messageType);
        return null;
      case CREATE_ACCOUNT:
        Requests.verify(false, ErrorCode.CREATE_ACCOUNT_IS_DEPRECATED);
        break;
      case ADMIN:
        return handleAdminAction(source);
      default:
        throw new ProgrammingError("Uncovered message type: " + messageType);
    }
    throw new ProgrammingError("This should never be reached.");
  }

  @NotNull
  private ClientMessage handleAvatar() {
    // FIXME implement?
    return new AvatarMessage();
  }

  private ClientMessage handleAdminAction(Map<String, Object> source) {
    switch ((String) source.get("action")) {
      case "closeFA":
        return new DisconnectPeerRequest(((Double) source.get("user_id")).intValue());
      case "closeLobby":
        return new DisconnectClientRequest(((Double) source.get("user_id")).intValue());
      case "requestavatars":
        return new GetAvatarsAdminRequest();
      case "remove_avatar":
        return new RemoveAvatarAdminRequest(((Double) source.get("idavatar")).intValue(), ((Double) source.get("iduser")).intValue());
      case "add_avatar":
        return new AddAvatarAdminRequest(((Double) source.get("idavatar")).intValue(), ((Double) source.get("iduser")).intValue());
      case "broadcast":
        return new BroadcastRequest((String) source.get("message"));
      default:
        Requests.verify(false, ErrorCode.UNKNOWN_MESSAGE, source);
        return null;
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
      Duration.ofSeconds(((Double) args.get(0)).intValue()),
      ((Double) args.get(0)).intValue(),
      (String) args.get(1),
      ((Double) args.get(2)).intValue(),
      (String) args.get(3)
    );
  }

  private ClientMessage handleJsonStats(Map<String, Object> source) {
    return noCatch(() -> {
      JsonNode node = objectMapper.readTree((String) getArgs(source).get(0));
      JsonNode stats = node.get("stats");
      TypeReference<List<ArmyStatistics>> typeReference = new TypeReference<List<ArmyStatistics>>() {
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
      ((Double) args.get(0)).intValue() == 1,
      ((Double) args.get(1)).intValue() == 1,
      Duration.ofSeconds(((Double) args.get(2)).intValue())
    );
  }

  private ClientMessage handleGameResult(Map<String, Object> source) {
    List<Object> args;
    args = getArgs(source);
    int armyId = ((Double) args.get(0)).intValue();
    String[] results = ((String) args.get(1)).split(" ");

    if ("score".equals(results[0])) {
      return new ArmyScoreReport(armyId, Integer.parseInt(results[1]));
    }

    Outcome outcome = Outcome.fromString(results[0]);
    return new ArmyOutcomeReport(armyId, outcome);
  }

  private ClientMessage handleGameMods(Map<String, Object> source) {
    List<Object> args;
    args = getArgs(source);
    switch ((String) args.get(0)) {
      case "activated":
        return new GameModsCountReport(((Double) args.get(1)).intValue());
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
    return new LoginMessage(
      (String) source.get("login"),
      (String) source.get("password"),
      (String) source.get("unique_id"));
  }

  private ClientMessage handleSocialRemove(Map<String, Object> source) {
    if (source.containsKey("friend")) {
      return new RemoveFriendRequest(((Double) source.get("friend")).intValue());
    } else if (source.containsKey("foe")) {
      return new RemoveFoeRequest(((Double) source.get("foe")).intValue());
    }
    Requests.verify(false, ErrorCode.UNKNOWN_MESSAGE, source);
    return null;
  }

  private ClientMessage handleSocialAdd(Map<String, Object> source) {
    if (source.containsKey("friend")) {
      return new AddFriendRequest(((Double) source.get("friend")).intValue());
    } else if (source.containsKey("foe")) {
      return new AddFoeRequest(((Double) source.get("foe")).intValue());
    }
    Requests.verify(false, ErrorCode.UNKNOWN_MESSAGE, source);
    return null;
  }

  private ClientMessage handleHostGame(Map<String, Object> source) {
    return new HostGameRequest(
      (String) source.get("mapname"),
      (String) source.get("title"),
      (String) source.get("mod"),
      GameAccess.fromString((String) source.get("access")),
      source.get("version") == null ? null : ((Double) source.get("version")).intValue(),
      (String) source.get("password"),
      GameVisibility.fromString((String) source.get("visibility")),
      source.get("minRating") == null ? null : ((Double) source.get("minRating")).intValue(),
      source.get("maxRating") == null ? null : ((Double) source.get("maxRating")).intValue()
    );
  }

  @SuppressWarnings("unchecked")
  private List<Object> getArgs(Map<String, Object> source) {
    return (List<Object>) source.get("args");
  }
}
