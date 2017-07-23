package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.avatar.AddAvatarAdminRequest;
import com.faforever.server.avatar.GetAvatarsAdminRequest;
import com.faforever.server.avatar.RemoveAvatarAdminRequest;
import com.faforever.server.client.BroadcastRequest;
import com.faforever.server.client.LegacyLoginRequest;
import com.faforever.server.client.LegacySessionRequest;
import com.faforever.server.coop.CoopMissionCompletedReport;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.game.AiOptionReport;
import com.faforever.server.game.ArmyOutcomeReport;
import com.faforever.server.game.ArmyScoreReport;
import com.faforever.server.game.BottleneckClearedReport;
import com.faforever.server.game.BottleneckReport;
import com.faforever.server.game.ClearSlotRequest;
import com.faforever.server.game.DesyncReport;
import com.faforever.server.game.DisconnectPeerRequest;
import com.faforever.server.game.DisconnectedReport;
import com.faforever.server.game.Faction;
import com.faforever.server.game.GameModsCountReport;
import com.faforever.server.game.GameModsReport;
import com.faforever.server.game.GameOptionReport;
import com.faforever.server.game.GameStateReport;
import com.faforever.server.game.GameVisibility;
import com.faforever.server.game.HostGameRequest;
import com.faforever.server.game.JoinGameRequest;
import com.faforever.server.game.Outcome;
import com.faforever.server.game.PlayerDefeatedReport;
import com.faforever.server.game.PlayerGameState;
import com.faforever.server.game.PlayerOptionReport;
import com.faforever.server.game.TeamKillReport;
import com.faforever.server.ice.IceMessage;
import com.faforever.server.ice.IceServersRequest;
import com.faforever.server.matchmaker.MatchMakerCancelRequest;
import com.faforever.server.matchmaker.MatchMakerSearchRequest;
import com.faforever.server.social.AddFoeRequest;
import com.faforever.server.social.AddFriendRequest;
import com.faforever.server.social.RemoveFoeRequest;
import com.faforever.server.social.RemoveFriendRequest;
import com.faforever.server.stats.ArmyStatisticsReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static com.faforever.server.error.RequestExceptionWithCode.requestExceptionWithCode;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

// TODO more testing needed
public class LegacyRequestTransformerTest {
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final String KEY_COMMAND = "command";
  private static final String KEY_ACTION = "action";
  private static final String KEY_ARGS = "args";
  private static final String COMMAND_ADMIN = "admin";
  private static final String TEST_USERNAME = "JUnit";
  private static final String TEST_PASSWORD = "secret";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private LegacyRequestTransformer instance;

  @Before
  public void setUp() throws Exception {
    instance = new LegacyRequestTransformer(objectMapper);
  }

  @Test
  public void transformHostGameRequest() throws Exception {
    HostGameRequest hostGameRequest = (HostGameRequest) instance.transform(ImmutableMap.<String, Object>builder()
      .put(KEY_COMMAND, "game_host")
      .put("mapname", "SCMP_001")
      .put("title", "Test")
      .put("mod", "faf")
      .put("access", "private")
      .put("password", TEST_PASSWORD)
      .put("visibility", "public")
      .put("minRating", 1000.0) // Because JSON deserializes integer values to Double
      .put("maxRating", 1500.0) // Because JSON deserializes integer values to Double
      .build()
    );

    assertThat(hostGameRequest, is(notNullValue()));
    assertThat(hostGameRequest.getMapName(), is("SCMP_001"));
    assertThat(hostGameRequest.getTitle(), is("Test"));
    assertThat(hostGameRequest.getMod(), is("faf"));
    assertThat(hostGameRequest.getPassword(), is(TEST_PASSWORD));
    assertThat(hostGameRequest.getVisibility(), is(GameVisibility.PUBLIC));
    assertThat(hostGameRequest.getMinRating(), is(1000));
    assertThat(hostGameRequest.getMaxRating(), is(1500));
  }

  @Test
  public void transformJoinGame() throws Exception {
    JoinGameRequest joinGameRequest = (JoinGameRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "game_join",
      "uid", 123,
      "password", TEST_PASSWORD
    ));

    assertThat(joinGameRequest, is(notNullValue()));
    assertThat(joinGameRequest.getId(), is(123));
    assertThat(joinGameRequest.getPassword(), is(TEST_PASSWORD));
  }

  @Test
  public void transformAskSession() throws Exception {
    LegacySessionRequest sessionRequest = (LegacySessionRequest) instance.transform(ImmutableMap.of(KEY_COMMAND, "ask_session"));

    assertThat(sessionRequest, is(notNullValue()));
  }

  @Test
  public void transformAddFriend() throws Exception {
    AddFriendRequest addFriendRequest = (AddFriendRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "social_add",
      "friend", 123
    ));

    assertThat(addFriendRequest, is(notNullValue()));
    assertThat(addFriendRequest.getPlayerId(), is(123));
  }

  @Test
  public void transformAddFoe() throws Exception {
    AddFoeRequest addFoeRequest = (AddFoeRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "social_add",
      "foe", 123
    ));

    assertThat(addFoeRequest, is(notNullValue()));
    assertThat(addFoeRequest.getPlayerId(), is(123));
  }

  @Test
  public void transformRemoveFriend() throws Exception {
    RemoveFriendRequest request = (RemoveFriendRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "social_remove",
      "friend", 123
    ));

    assertThat(request, is(notNullValue()));
    assertThat(request.getPlayerId(), is(123));
  }

  @Test
  public void transformRemoveFoe() throws Exception {
    RemoveFoeRequest request = (RemoveFoeRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "social_remove",
      "foe", 123
    ));

    assertThat(request, is(notNullValue()));
    assertThat(request.getPlayerId(), is(123));
  }

  @Test
  public void invalidSocialRemove() throws Exception {
    expectedException.expect(requestExceptionWithCode(ErrorCode.UNSUPPORTED_REQUEST));
    instance.transform(ImmutableMap.of(
      KEY_COMMAND, "social_remove",
      "foo", "bar" // Because JSON deserializes untyped integer values to Double
    ));
  }

  @Test
  public void invalidSocialAdd() throws Exception {
    expectedException.expect(requestExceptionWithCode(ErrorCode.UNSUPPORTED_REQUEST));
    instance.transform(ImmutableMap.of(
      KEY_COMMAND, "social_add",
      "foo", "bar"
    ));
  }

  @Test
  public void transformLogin() throws Exception {
    LegacyLoginRequest loginRequest = (LegacyLoginRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "hello",
      "login", TEST_USERNAME,
      "password", TEST_PASSWORD,
      "unique_id", "foobar"
    ));

    assertThat(loginRequest, is(notNullValue()));
    assertThat(loginRequest.getLogin(), is(TEST_USERNAME));
    assertThat(loginRequest.getPassword(), is(TEST_PASSWORD));
    assertThat(loginRequest.getUniqueId(), is("foobar"));
  }

  @Test
  public void transformGameResultToArmyScoreReport() throws Exception {
    ArmyScoreReport armyScoreReport = (ArmyScoreReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "GameResult",
      KEY_ARGS, Arrays.asList(1, "score 10")
    ));

    assertThat(armyScoreReport, is(notNullValue()));
    assertThat(armyScoreReport.getArmyId(), is(1));
    assertThat(armyScoreReport.getScore(), is(10));
  }

  @Test
  public void transformGameResultToArmyOutcomeReport() throws Exception {
    ArmyOutcomeReport armyOutcomeReport = (ArmyOutcomeReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "GameResult",
      KEY_ARGS, Arrays.asList(1, "victory")
    ));

    assertThat(armyOutcomeReport, is(notNullValue()));
    assertThat(armyOutcomeReport.getArmyId(), is(1));
    assertThat(armyOutcomeReport.getOutcome(), is(Outcome.VICTORY));
  }

  @Test
  public void transformGameState() throws Exception {
    GameStateReport gameStateReport = (GameStateReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "GameState",
      KEY_ARGS, Collections.singletonList("Lobby")
    ));

    assertThat(gameStateReport, is(notNullValue()));
    assertThat(gameStateReport.getState(), is(PlayerGameState.LOBBY));
  }

  @Test
  public void transformGameOption() throws Exception {
    GameOptionReport gameOptionReport = (GameOptionReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "GameOption",
      KEY_ARGS, Arrays.asList("GameSpeed", "normal")
    ));

    assertThat(gameOptionReport, is(notNullValue()));
    assertThat(gameOptionReport.getKey(), is("GameSpeed"));
    assertThat(gameOptionReport.getValue(), is("normal"));
  }

  @Test
  public void transformGameMods() throws Exception {
    GameModsReport gameModsReport = (GameModsReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "GameMods",
      KEY_ARGS, Arrays.asList("uids", "1-2-3-4 5-6-7-8")
    ));

    assertThat(gameModsReport, is(notNullValue()));
    assertThat(gameModsReport.getModUids(), contains("1-2-3-4", "5-6-7-8"));
  }

  @Test
  public void transformGameModsActivated() throws Exception {
    GameModsCountReport gameModsCountReport = (GameModsCountReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "GameMods",
      KEY_ARGS, Arrays.asList("activated", 0d)
    ));

    assertThat(gameModsCountReport, is(notNullValue()));
    assertThat(gameModsCountReport.getCount(), is(0));
  }

  @Test
  public void transformPlayerOption() throws Exception {
    PlayerOptionReport playerOptionReport = (PlayerOptionReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "PlayerOption",
      KEY_ARGS, Arrays.asList("1", "Faction", 3)
    ));

    assertThat(playerOptionReport, is(notNullValue()));
    assertThat(playerOptionReport.getPlayerId(), is(1));
    assertThat(playerOptionReport.getKey(), is("Faction"));
    assertThat(playerOptionReport.getValue(), is(3));
  }

  @Test
  public void transformClearSlot() throws Exception {
    ClearSlotRequest clearSlotRequest = (ClearSlotRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "ClearSlot",
      KEY_ARGS, Collections.singletonList(1)
    ));

    assertThat(clearSlotRequest, is(notNullValue()));
    assertThat(clearSlotRequest.getSlotId(), is(1));
  }

  @Test
  public void transformOperationComplete() throws Exception {
    CoopMissionCompletedReport coopMissionCompletedReport = (CoopMissionCompletedReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "OperationComplete",
      KEY_ARGS, Arrays.asList(1, 0, 1440)
    ));

    assertThat(coopMissionCompletedReport, is(notNullValue()));
    assertThat(coopMissionCompletedReport.isPrimaryTargets(), is(true));
    assertThat(coopMissionCompletedReport.isSecondaryTargets(), is(false));
    assertThat(coopMissionCompletedReport.getTime(), is(Duration.ofSeconds(1440)));
  }

  @Test
  public void transformJsonStats() throws Exception {
    String stats;
    try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/stats/game_stats_full_example.json"), StandardCharsets.UTF_8)) {
      stats = CharStreams.toString(reader);
    }

    ArmyStatisticsReport armyStatisticsReport = (ArmyStatisticsReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "JsonStats",
      KEY_ARGS, Collections.singletonList(stats)
    ));

    assertThat(armyStatisticsReport, is(notNullValue()));
    assertThat(armyStatisticsReport.getArmyStatistics(), is(notNullValue()));
  }

  @Test
  public void transformPlayerDefeated() throws Exception {
    PlayerDefeatedReport playerDefeatedReport = (PlayerDefeatedReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "EnforceRating"
    ));

    assertThat(playerDefeatedReport, is(notNullValue()));
  }

  @Test
  public void transformAiOption() throws Exception {
    AiOptionReport aiOptionReport = (AiOptionReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "AIOption",
      KEY_ARGS, Arrays.asList("QAI", "Faction", 3)
    ));

    assertThat(aiOptionReport, is(notNullValue()));
    assertThat(aiOptionReport.getAiName(), is("QAI"));
    assertThat(aiOptionReport.getKey(), is("Faction"));
    assertThat(aiOptionReport.getValue(), is(3));
  }

  @Test
  public void teamKillReport() throws Exception {
    TeamKillReport teamKillReport = (TeamKillReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "TeamkillReport",
      KEY_ARGS, Arrays.asList(1, TEST_USERNAME, 2, "TestNG")
    ));

    assertThat(teamKillReport.getVictimId(), is(1));
    assertThat(teamKillReport.getVictimName(), is(TEST_USERNAME));
    assertThat(teamKillReport.getKillerId(), is(2));
    assertThat(teamKillReport.getKillerName(), is("TestNG"));
  }

  @Test
  public void aiOption() throws Exception {
    AiOptionReport report = (AiOptionReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "AIOption",
      KEY_ARGS, Arrays.asList("QAI", "Team", 1)
    ));

    assertThat(report.getAiName(), is("QAI"));
    assertThat(report.getKey(), is("Team"));
    assertThat(report.getValue(), is(1));
  }

  @Test
  public void createAccountThrowsException() throws Exception {
    expectedException.expect(requestExceptionWithCode(ErrorCode.CREATE_ACCOUNT_IS_DEPRECATED));
    instance.transform(ImmutableMap.of(
      KEY_COMMAND, "create_account"
    ));
  }

  @Test
  public void desync() throws Exception {
    DesyncReport report = (DesyncReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "Desync"
    ));

    assertThat(report, is(notNullValue()));
  }

  @Test
  public void disconnected() throws Exception {
    DisconnectedReport report = (DisconnectedReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "Disconnected"
    ));

    assertThat(report, is(notNullValue()));
  }

  @Test
  public void bottleneck() throws Exception {
    BottleneckReport report = (BottleneckReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "Bottleneck"
    ));

    assertThat(report, is(notNullValue()));
  }

  @Test
  public void bottleneckCleared() throws Exception {
    BottleneckClearedReport report = (BottleneckClearedReport) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "BottleneckCleared"
    ));

    assertThat(report, is(notNullValue()));
  }

  @Test
  public void closeFa() throws Exception {
    DisconnectPeerRequest request = (DisconnectPeerRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, COMMAND_ADMIN,
      KEY_ACTION, "closeFA",
      "user_id", 1
    ));

    assertThat(request.getPlayerId(), is(1));
  }

  @Test
  public void requestAvatars() throws Exception {
    GetAvatarsAdminRequest request = (GetAvatarsAdminRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, COMMAND_ADMIN,
      KEY_ACTION, "requestavatars"
    ));

    assertThat(request, is(notNullValue()));
  }

  @Test
  public void removeAvatar() throws Exception {
    RemoveAvatarAdminRequest request = (RemoveAvatarAdminRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, COMMAND_ADMIN,
      KEY_ACTION, "remove_avatar",
      "iduser", 3d,
      "idavatar", 5d
    ));

    assertThat(request.getPlayerId(), is(3));
    assertThat(request.getAvatarId(), is(5));
  }

  @Test
  public void addAvatar() throws Exception {
    AddAvatarAdminRequest request = (AddAvatarAdminRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, COMMAND_ADMIN,
      KEY_ACTION, "add_avatar",
      "iduser", 3d,
      "idavatar", 5d
    ));

    assertThat(request.getPlayerId(), is(3));
    assertThat(request.getAvatarId(), is(5));
  }

  @Test
  public void broadcastRequest() throws Exception {
    BroadcastRequest request = (BroadcastRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, COMMAND_ADMIN,
      KEY_ACTION, "broadcast",
      "message", "Hello world"
    ));

    assertThat(request.getMessage(), is("Hello world"));
  }

  @Test
  public void unknownAdminAction() throws Exception {
    expectedException.expect(requestExceptionWithCode(ErrorCode.UNSUPPORTED_REQUEST));
    instance.transform(ImmutableMap.of(
      KEY_COMMAND, COMMAND_ADMIN,
      KEY_ACTION, "something"
    ));
  }

  @Test
  public void searchMatchMakingFactionString() throws Exception {
    MatchMakerSearchRequest result = (MatchMakerSearchRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "game_matchmaking",
      "mod", "ladder1v1",
      "faction", "seraphim",
      "state", "start"
    ));

    assertThat(result.getPoolName(), is("ladder1v1"));
    assertThat(result.getFaction(), is(Faction.SERAPHIM));
  }

  @Test
  public void searchMatchMakingFactionInteger() throws Exception {
    MatchMakerSearchRequest result = (MatchMakerSearchRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "game_matchmaking",
      "mod", "ladder1v1",
      "faction", Faction.SERAPHIM.toFaValue(),
      "state", "start"
    ));

    assertThat(result.getPoolName(), is("ladder1v1"));
    assertThat(result.getFaction(), is(Faction.SERAPHIM));
  }

  @Test
  public void stopMatchMaking() throws Exception {
    MatchMakerCancelRequest result = (MatchMakerCancelRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "game_matchmaking",
      "mod", "ladder1v1",
      "state", "stop"
    ));

    assertThat(result.getPoolName(), is("ladder1v1"));
  }

  @Test
  public void requestIceServers() throws Exception {
    IceServersRequest result = (IceServersRequest) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "ice_servers"
    ));

    assertThat(result, is(instanceOf(IceServersRequest.class)));
  }

  @Test
  public void iceMessage() throws Exception {
    IceMessage iceMessage = (IceMessage) instance.transform(ImmutableMap.of(
      KEY_COMMAND, "IceMsg",
      KEY_ARGS, Arrays.asList(1, "someObject")
    ));

    assertThat(iceMessage.getReceiverId(), is(1));
    assertThat(iceMessage.getContent(), is("someObject"));
  }

  @Test
  public void restoreGameSession() throws Exception {
    RestoreGameSessionRequest result = (RestoreGameSessionRequest) instance.transform(ImmutableMap.of(
      "command", "restore_game_session",
      "game_id", 5
    ));

    assertThat(result.getGameId(), is(5));
  }
}
