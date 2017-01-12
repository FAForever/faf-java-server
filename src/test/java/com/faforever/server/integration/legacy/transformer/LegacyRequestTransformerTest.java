package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.SessionRequest;
import com.faforever.server.coop.CoopMissionCompletedReport;
import com.faforever.server.game.*;
import com.faforever.server.integration.request.GameStateReport;
import com.faforever.server.integration.request.HostGameRequest;
import com.faforever.server.security.LoginMessage;
import com.faforever.server.social.AddFoeMessage;
import com.faforever.server.social.AddFriendMessage;
import com.faforever.server.statistics.ArmyStatisticsReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

// TODO more testing needed
public class LegacyRequestTransformerTest {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private LegacyRequestTransformer instance;

  @Before
  public void setUp() throws Exception {
    instance = new LegacyRequestTransformer(objectMapper);
  }

  @Test
  public void transformHostGameRequest() throws Exception {
    HostGameRequest hostGameRequest = (HostGameRequest) instance.transform(ImmutableMap.<String, Object>builder()
      .put("command", "game_host")
      .put("mapname", "SCMP_001")
      .put("title", "Test")
      .put("mod", "faf")
      .put("access", "private")
      .put("version", 1337.0) // Because JSON deserializes integer values to Double
      .put("password", "secret")
      .put("visibility", "public")
      .build()
    );

    assertThat(hostGameRequest, is(notNullValue()));
    assertThat(hostGameRequest.getMapName(), is("SCMP_001"));
    assertThat(hostGameRequest.getTitle(), is("Test"));
    assertThat(hostGameRequest.getMod(), is("faf"));
    assertThat(hostGameRequest.getAccess(), is(GameAccess.PRIVATE));
    assertThat(hostGameRequest.getVersion(), is(1337));
    assertThat(hostGameRequest.getPassword(), is("secret"));
    assertThat(hostGameRequest.getVisibility(), is(GameVisibility.PUBLIC));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void transformListReplays() throws Exception {
    instance.transform(ImmutableMap.of("command", "list"));
  }

  @Test
  public void transformJoinGame() throws Exception {
    JoinGameRequest joinGameRequest = (JoinGameRequest) instance.transform(ImmutableMap.of(
      "command", "game_join",
      "uid", 123.0, // Because JSON deserializes untyped integer values to Double
      "password", "secret"
    ));

    assertThat(joinGameRequest, is(notNullValue()));
    assertThat(joinGameRequest.getId(), is(123));
    assertThat(joinGameRequest.getPassword(), is("secret"));
  }

  @Test
  public void transformAskSession() throws Exception {
    SessionRequest sessionRequest = (SessionRequest) instance.transform(ImmutableMap.of("command", "ask_session"));

    assertThat(sessionRequest, is(notNullValue()));
  }

  @Test
  public void transformAddFriend() throws Exception {
    AddFriendMessage addFriendMessage = (AddFriendMessage) instance.transform(ImmutableMap.of(
      "command", "social_add",
      "friend", 123.0 // Because JSON deserializes untyped integer values to Double
    ));

    assertThat(addFriendMessage, is(notNullValue()));
    assertThat(addFriendMessage.getPlayerId(), is(123));
  }

  @Test
  public void transformAddFoe() throws Exception {
    AddFoeMessage addFoeMessage = (AddFoeMessage) instance.transform(ImmutableMap.of(
      "command", "social_add",
      "foe", 123.0 // Because JSON deserializes untyped integer values to Double
    ));

    assertThat(addFoeMessage, is(notNullValue()));
    assertThat(addFoeMessage.getPlayerId(), is(123));
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidSocialAdd() throws Exception {
    instance.transform(ImmutableMap.of("command", "social_add"));
  }

  @Test
  public void transformLogin() throws Exception {
    LoginMessage loginMessage = (LoginMessage) instance.transform(ImmutableMap.of(
      "command", "hello",
      "login", "JUnit",
      "password", "secret",
      "unique_id", "foobar"
    ));

    assertThat(loginMessage, is(notNullValue()));
    assertThat(loginMessage.getLogin(), is("JUnit"));
    assertThat(loginMessage.getPassword(), is("secret"));
    assertThat(loginMessage.getUniqueId(), is("foobar"));
  }

  @Test
  public void transformGameResultToArmyScoreReport() throws Exception {
    ArmyScoreReport armyScoreReport = (ArmyScoreReport) instance.transform(ImmutableMap.of(
      "command", "GameResult",
      "args", Arrays.asList(1, "score 10")
    ));

    assertThat(armyScoreReport, is(notNullValue()));
    assertThat(armyScoreReport.getArmyId(), is(1));
    assertThat(armyScoreReport.getScore(), is(10));
  }

  @Test
  public void transformGameResultToArmyOutcomeReport() throws Exception {
    ArmyOutcomeReport armyOutcomeReport = (ArmyOutcomeReport) instance.transform(ImmutableMap.of(
      "command", "GameResult",
      "args", Arrays.asList(1, "victory")
    ));

    assertThat(armyOutcomeReport, is(notNullValue()));
    assertThat(armyOutcomeReport.getArmyId(), is(1));
    assertThat(armyOutcomeReport.getOutcome(), is(Outcome.VICTORY));
  }

  @Test
  public void transformGameState() throws Exception {
    GameStateReport gameStateReport = (GameStateReport) instance.transform(ImmutableMap.of(
      "command", "GameState",
      "args", Collections.singletonList("Lobby")
    ));

    assertThat(gameStateReport, is(notNullValue()));
    assertThat(gameStateReport.getGameState(), is(GameState.LOBBY));
  }

  @Test
  public void transformGameOption() throws Exception {
    GameOptionReport gameOptionReport = (GameOptionReport) instance.transform(ImmutableMap.of(
      "command", "GameOption",
      "args", Arrays.asList("GameSpeed", "normal")
    ));

    assertThat(gameOptionReport, is(notNullValue()));
    assertThat(gameOptionReport.getKey(), is("GameSpeed"));
    assertThat(gameOptionReport.getValue(), is("normal"));
  }

  @Test
  public void transformGameMods() throws Exception {
    GameModsReport gameModsReport = (GameModsReport) instance.transform(ImmutableMap.of(
      "command", "GameMods",
      "args", Arrays.asList("uids", "1-2-3-4 5-6-7-8")
    ));

    assertThat(gameModsReport, is(notNullValue()));
    assertThat(gameModsReport.getModUids(), contains("1-2-3-4", "5-6-7-8"));
  }

  @Test
  public void transformGameModsActivated() throws Exception {
    GameModsCountReport gameModsCountReport = (GameModsCountReport) instance.transform(ImmutableMap.of(
      "command", "GameMods",
      "args", Arrays.asList("activated", 0)
    ));

    assertThat(gameModsCountReport, is(notNullValue()));
    assertThat(gameModsCountReport.getCount(), is(0));
  }

  @Test
  public void transformPlayerOption() throws Exception {
    PlayerOptionReport playerOptionReport = (PlayerOptionReport) instance.transform(ImmutableMap.of(
      "command", "PlayerOption",
      "args", Arrays.asList("1", "Faction", 3)
    ));

    assertThat(playerOptionReport, is(notNullValue()));
    assertThat(playerOptionReport.getPlayerId(), is(1));
    assertThat(playerOptionReport.getKey(), is("Faction"));
    assertThat(playerOptionReport.getValue(), is(3));
  }

  @Test
  public void transformClearSlot() throws Exception {
    ClearSlotRequest clearSlotRequest = (ClearSlotRequest) instance.transform(ImmutableMap.of(
      "command", "ClearSlot",
      "args", Collections.singletonList(1)
    ));

    assertThat(clearSlotRequest, is(notNullValue()));
    assertThat(clearSlotRequest.getSlotId(), is(1));
  }

  @Test
  public void transformOperationComplete() throws Exception {
    CoopMissionCompletedReport coopMissionCompletedReport = (CoopMissionCompletedReport) instance.transform(ImmutableMap.of(
      "command", "OperationComplete",
      "args", Arrays.asList(1, 0, 1440)
    ));

    assertThat(coopMissionCompletedReport, is(notNullValue()));
    assertThat(coopMissionCompletedReport.isPrimaryTargets(), is(true));
    assertThat(coopMissionCompletedReport.isSecondaryTargets(), is(false));
    assertThat(coopMissionCompletedReport.getDuration(), is(Duration.ofSeconds(1440)));
  }

  @Test
  public void transformJsonStats() throws Exception {
    String stats;
    try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/stats/game_stats_full_example.json"), StandardCharsets.UTF_8)) {
      stats = CharStreams.toString(reader);
    }

    ArmyStatisticsReport armyStatisticsReport = (ArmyStatisticsReport) instance.transform(ImmutableMap.of(
      "command", "JsonStats",
      "args", Collections.singletonList(stats)
    ));

    assertThat(armyStatisticsReport, is(notNullValue()));
    assertThat(armyStatisticsReport.getArmyStatistics(), is(notNullValue()));
  }

  @Test
  public void transformEnforceRating() throws Exception {
    EnforceRatingRequest enforceRatingRequest = (EnforceRatingRequest) instance.transform(ImmutableMap.of(
      "command", "EnforceRating"
    ));

    assertThat(enforceRatingRequest, is(notNullValue()));
  }

  @Test
  public void transformAiOption() throws Exception {
    AiOptionReport aiOptionReport = (AiOptionReport) instance.transform(ImmutableMap.of(
      "command", "AIOption",
      "args", Arrays.asList("QAI", "Faction", 3)
    ));

    assertThat(aiOptionReport, is(notNullValue()));
    assertThat(aiOptionReport.getAiName(), is("QAI"));
    assertThat(aiOptionReport.getKey(), is("Faction"));
    assertThat(aiOptionReport.getValue(), is(3));
  }
}
