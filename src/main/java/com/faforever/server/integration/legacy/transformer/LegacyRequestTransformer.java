package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.avatar.AddAvatarAdminRequest;
import com.faforever.server.avatar.AvatarMessage;
import com.faforever.server.avatar.GetAvatarsAdminRequest;
import com.faforever.server.avatar.RemoveAvatarAdminRequest;
import com.faforever.server.client.BroadcastRequest;
import com.faforever.server.client.CloseClientRequest;
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
import com.faforever.server.game.CloseGameRequest;
import com.faforever.server.game.DesyncReport;
import com.faforever.server.game.EnforceRatingRequest;
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
import com.faforever.server.matchmaker.MatchmakerMessage;
import com.faforever.server.request.ClientMessage;
import com.faforever.server.social.AddFoeMessage;
import com.faforever.server.social.AddFriendMessage;
import com.faforever.server.social.SocialRemoveMessage;
import com.faforever.server.statistics.ArmyStatistics;
import com.faforever.server.statistics.ArmyStatisticsReport;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
        return new HostGameRequest(
          (String) source.get("mapname"),
          (String) source.get("title"),
          (String) source.get("mod"),
          GameAccess.fromString((String) source.get("access")),
          source.get("version") == null ? null : ((Double) source.get("version")).intValue(),
          (String) source.get("password"),
          GameVisibility.fromString((String) source.get("visibility")));

      case JOIN_GAME:
        return new JoinGameRequest(((Double) source.get("uid")).intValue(), (String) source.get("password"));

      case ASK_SESSION:
        return new SessionRequest();

      case SOCIAL_ADD:
        if (source.containsKey("friend")) {
          return new AddFriendMessage(((Double) source.get("friend")).intValue());
        } else if (source.containsKey("foe")) {
          return new AddFoeMessage(((Double) source.get("foe")).intValue());
        }
        throw new IllegalArgumentException("Invalid social_add message: " + source);

      case SOCIAL_REMOVE:
        return new SocialRemoveMessage();

      case LOGIN:
        return new LoginMessage(
          (String) source.get("login"),
          (String) source.get("password"),
          (String) source.get("unique_id"));

      case GAME_MATCH_MAKING:
        // FIXME implement
        return new MatchmakerMessage();

      case AVATAR:
        // FIXME implement
        return new AvatarMessage();

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
        args = getArgs(source);
        switch ((String) args.get(0)) {
          case "activated":
            return new GameModsCountReport((int) args.get(1));
          case "uids":
            return new GameModsReport(Arrays.asList(((String) args.get(1)).split(" ")));
        }
        throw new IllegalArgumentException("Unknown GameMods argument: " + args.get(0));

      case GAME_RESULT:
        args = getArgs(source);
        int armyId = (int) args.get(0);
        String[] results = ((String) args.get(1)).split(" ");

        if ("score".equals(results[0])) {
          return new ArmyScoreReport(armyId, Integer.parseInt(results[1]));
        }

        Outcome outcome = Outcome.fromString(results[0]);
        return new ArmyOutcomeReport(armyId, outcome);

      case OPERATION_COMPLETE:
        args = getArgs(source);
        return new CoopMissionCompletedReport((int) args.get(0) == 1, (int) args.get(1) == 1, Duration.ofSeconds((Integer) args.get(2)));

      case JSON_STATS:
        return noCatch(() -> {
          JsonNode node = objectMapper.readTree((String) getArgs(source).get(0));
          JsonNode stats = node.get("stats");
          TypeReference<List<ArmyStatistics>> typeReference = new TypeReference<List<ArmyStatistics>>() {
          };
          JsonParser jsonParser = stats.traverse();
          jsonParser.setCodec(objectMapper);
          return new ArmyStatisticsReport(jsonParser.readValueAs(typeReference));
        });

      case ENFORCE_RATING:
        return new EnforceRatingRequest();

      case TEAMKILL_REPORT:
        args = getArgs(source);
        // FIXME implement
        return new TeamKillReport(null, 0, "", 0, "");

      case AI_OPTION:
        args = getArgs(source);
        return new AiOptionReport((String) args.get(0), (String) args.get(1), args.get(2));

      case INITIATE_TEST:
        log.warn("Ignoring " + messageType);
        return null;

      case CREATE_ACCOUNT:
        Requests.verify(false, ErrorCode.CREATE_ACCOUNT_IS_DEPRECATED);

      case ADMIN:
        switch ((String) source.get("action")) {
          case "closeFA":
            return new CloseGameRequest((Integer) source.get("user_id"));
          case "closeLobby":
            return new CloseClientRequest((Integer) source.get("user_id"));
          case "requestavatars":
            return new GetAvatarsAdminRequest();
          case "remove_avatar":
            return new RemoveAvatarAdminRequest((int) source.get("idavatar"), (int) source.get("iduser"));
          case "add_avatar":
            return new AddAvatarAdminRequest((int) source.get("idavatar"), (int) source.get("iduser"));
          case "broadcast":
            return new BroadcastRequest((String) source.get("message"));
        }

      default:
        throw new ProgrammingError("Uncovered message type: " + messageType);
    }
  }

  @SuppressWarnings("unchecked")
  private List<Object> getArgs(Map<String, Object> source) {
    return (List<Object>) source.get("args");
  }
}
