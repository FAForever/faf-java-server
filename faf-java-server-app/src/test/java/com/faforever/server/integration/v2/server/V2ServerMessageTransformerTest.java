package com.faforever.server.integration.v2.server;

import com.faforever.server.api.dto.AchievementState;
import com.faforever.server.chat.JoinChatChannelResponse;
import com.faforever.server.client.ConnectToPeerResponse;
import com.faforever.server.client.DisconnectPlayerFromGameResponse;
import com.faforever.server.client.IceServersResponse;
import com.faforever.server.client.InfoResponse;
import com.faforever.server.client.UpdatedAchievementsResponse;
import com.faforever.server.client.UpdatedAchievementsResponse.UpdatedAchievement;
import com.faforever.server.config.JacksonConfig;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.ErrorResponse;
import com.faforever.server.game.Faction;
import com.faforever.server.game.GameResponse;
import com.faforever.server.game.GameResponse.FeaturedModFileVersion;
import com.faforever.server.game.GameResponse.Player;
import com.faforever.server.game.GameResponse.SimMod;
import com.faforever.server.game.GameState;
import com.faforever.server.game.GameVisibility;
import com.faforever.server.game.HostGameResponse;
import com.faforever.server.game.LobbyMode;
import com.faforever.server.game.StartGameProcessResponse;
import com.faforever.server.ice.IceServer;
import com.faforever.server.ice.IceServerList;
import com.faforever.server.matchmaker.MatchMakerResponse;
import com.faforever.server.mod.FeaturedModResponse;
import com.faforever.server.player.LoginDetailsResponse;
import com.faforever.server.player.PlayerResponse;
import com.faforever.server.player.PlayerResponse.Avatar;
import com.faforever.server.player.PlayerResponse.Rating;
import com.faforever.server.social.SocialRelationListResponse;
import com.faforever.server.social.SocialRelationListResponse.SocialRelationResponse;
import com.faforever.server.social.SocialRelationListResponse.SocialRelationResponse.RelationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.TimeZone;
import java.util.UUID;

import static com.spotify.hamcrest.jackson.JsonMatchers.isJsonStringMatching;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonArray;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonBoolean;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonDouble;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonInt;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonLong;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonObject;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@RunWith(MockitoJUnitRunner.class)
public class V2ServerMessageTransformerTest {

  private V2ServerMessageTransformer instance;

  @Before
  public void setUp() throws Exception {
    ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    instance = new V2ServerMessageTransformer(objectMapper, Mappers.getMapper(V2ServerMessageMapper.class));
  }

  @Test
  public void infoResponse() {
    String response = instance.transform(new InfoResponse("Hello JUnit"));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("info")))
        .where("data", is(
          jsonObject()
            .where("message", is(jsonText("Hello JUnit")))))));
  }

  @Test
  public void hostGame() {
    String response = instance.transform(new HostGameResponse("scmp01"));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("hostGame")))
        .where("data", is(
          jsonObject()
            .where("mapName", is(jsonText("scmp01")))))));
  }

  @Test
  public void joinChatChannel() {
    String response = instance.transform(new JoinChatChannelResponse(ImmutableSet.of("#one", "#two")));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("chatChannel")))
        .where("data", is(
          jsonObject().where("channels", is(jsonArray(contains(jsonText("#one"), jsonText("#two")))))))));
  }

  @Test
  public void connectToPeer() {
    String response = instance.transform(new ConnectToPeerResponse("junit", 123, false));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("connectToPeer")))
        .where("data", is(
          jsonObject()
            .where("playerName", is(jsonText("junit")))
            .where("playerId", is(jsonInt(123)))))));
  }

  @Test
  public void disconnectFromPlayer() {
    String response = instance.transform(new DisconnectPlayerFromGameResponse(123));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("disconnectFromPeer")))
        .where("data", is(
          jsonObject()
            .where("playerId", is(jsonInt(123)))))));
  }

  @Test
  public void featuredMod() {
    String response = instance.transform(new FeaturedModResponse("faf", "Forged Alliance Forever", "Description", 1));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("featuredMod")))
        .where("data", is(
          jsonObject()
            .where("technicalName", is(jsonText("faf")))
            .where("displayName", is(jsonText("Forged Alliance Forever")))
            .where("description", is(jsonText("Description")))
            .where("displayOrder", is(jsonInt(1)))
        ))));
  }

  @Test
  public void game() {
    Instant startTime = Instant.parse("2007-12-03T10:15:30.00Z");

    String response = instance.transform(new GameResponse(1, "Title", GameVisibility.PUBLIC, false, GameState.ENDED, "faf",
        Arrays.asList(
          new SimMod("1-1-1-1", "Mod #1"),
          new SimMod("2-2-2-2", "Mod #2")
        ),
        "scmp01", "JUnit4",
        Arrays.asList(
          new Player(1, "JUnit4", 1),
          new Player(2, "JUnit5", 1)
        ),
        12,
        startTime,
        500,
        800,
      1,
        Arrays.asList(
          new FeaturedModFileVersion((short) 1, 4444),
          new FeaturedModFileVersion((short) 2, 6789)
        )
      )
    );

    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("game")))
        .where("data", is(
          jsonObject()
            .where("id", is(jsonInt(1)))
            .where("title", is(jsonText("Title")))
            .where("gameVisibility", is(jsonText("PUBLIC")))
            .where("passwordProtected", is(jsonBoolean(false)))
            .where("state", is(jsonText("ENDED")))
            .where("mod", is(jsonText("faf")))
            .where("simMods", is(jsonArray(contains(
              jsonObject()
                .where("uid", is(jsonText("1-1-1-1")))
                .where("displayName", is(jsonText("Mod #1"))),
              jsonObject()
                .where("uid", is(jsonText("2-2-2-2")))
                .where("displayName", is(jsonText("Mod #2")))
            ))))
            .where("map", is(jsonText("scmp01")))
            .where("hostUsername", is(jsonText("JUnit4")))
            .where("players", is(jsonArray(contains(
              jsonObject()
                .where("id", is(jsonInt(1)))
                .where("team", is(jsonInt(1))),
              jsonObject()
                .where("id", is(jsonInt(2)))
                .where("team", is(jsonInt(1)))))))
            .where("maxPlayers", is(jsonInt(12)))
            .where("startTime", is(jsonLong(1196676930000L)))
            .where("minRating", is(jsonInt(500)))
            .where("maxRating", is(jsonInt(800)))
            .where("modFileVersions", is(jsonArray(contains(
              jsonObject()
                .where("id", is(jsonInt(1)))
                .where("version", is(jsonInt(4444))),
              jsonObject()
                .where("id", is(jsonInt(2)))
                .where("version", is(jsonInt(6789)))
            ))))
            .where("modVersion", is(jsonInt(1)))
        ))));
  }

  @Test
  public void iceServers() {
    Instant createdAt = Instant.parse("2007-12-03T10:15:30.00Z");

    String response = instance.transform(new IceServersResponse(Collections.singletonList(
      new IceServerList(3600, createdAt, Collections.singletonList(new IceServer(
        URI.create("http://localhost"),
        "anonymous",
        "123",
        "token"
      )))
    )));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("iceServers")))
        .where("data", is(
          jsonObject()
            .where("iceServerLists", is(jsonArray(contains(
              jsonObject()
                .where("ttlSeconds", is(jsonInt(3600)))
                .where("createdAt", is(jsonLong(1196676930000L)))
                .where("servers", is(jsonArray(contains(
                  jsonObject()
                    .where("url", is(jsonText("http://localhost")))
                    .where("username", is(jsonText("anonymous")))
                    .where("credential", is(jsonText("123")))
                    .where("credentialType", is(jsonText("token")))
                ))))
            ))))
        ))));
  }

  @Test
  public void loginDetails() {
    String response = instance.transform(new LoginDetailsResponse(new PlayerResponse(
      1,
      "A",
      "CH",
      TimeZone.getTimeZone("Europe/Berlin"),
      new Rating(900, 120),
      new Rating(600, 50),
      12,
      new Avatar("http://localhost/avatar.png", "Avatar"),
      "AA"
    )));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("loginDetails")))
        .where("data", is(
          jsonObject()
            .where("playerId", is(jsonInt(1)))
            .where("username", is(jsonText("A")))
            .where("country", is(jsonText("CH")))
            .where("timeZone", is(jsonText("Europe/Berlin")))
            .where("globalRating", is(
              jsonObject()
                .where("mean", is(jsonDouble(900.0)))
                .where("deviation", is(jsonDouble(120.0)))
            ))
            .where("ladder1v1Rating", is(
              jsonObject()
                .where("mean", is(jsonDouble(600.0)))
                .where("deviation", is(jsonDouble(50.0)))
            ))
            .where("numberOfGames", is(jsonInt(12)))
            .where("avatar", is(
              jsonObject()
                .where("url", is(jsonText("http://localhost/avatar.png")))
                .where("description", is(jsonText("Avatar")))
            ))
            .where("clanTag", is(jsonText("AA")))
        ))));
  }

  @Test
  public void matchAvailable() {
    String response = instance.transform(new MatchMakerResponse("ladder2v2"));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("matchAvailable")))
        .where("data", is(
          jsonObject()
            .where("pool", is(jsonText("ladder2v2")))
        ))));
  }

  @Test
  public void socialRelationList() {
    String response = instance.transform(new SocialRelationListResponse(Arrays.asList(
      new SocialRelationResponse(1, RelationType.FRIEND),
      new SocialRelationResponse(2, RelationType.FOE)
    )));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("socialRelations")))
        .where("data", is(
          jsonObject()
            .where("socialRelations", is(jsonArray(contains(
              jsonObject()
                .where("playerId", is(jsonInt(1)))
                .where("type", is(jsonText("FRIEND"))),
              jsonObject()
                .where("playerId", is(jsonInt(2)))
                .where("type", is(jsonText("FOE")))
            ))))
        ))));
  }

  @Test
  public void startGameProcessWithoutMap() {
    String response = instance.transform(new StartGameProcessResponse("faf", 1, null,
      LobbyMode.DEFAULT, Faction.UEF, "someName", 2, 1, 3, Arrays.asList("/foo", "/bar")));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("startGameProcess")))
        .where("data", is(
          jsonObject()
            .where("mod", is(jsonText("faf")))
            .where("gameId", is(jsonInt(1)))
            .where("lobbyMode", is(jsonText("DEFAULT")))
            .where("faction", is(jsonText("uef")))
            .where("name", is(jsonText("someName")))
            .where("expectedPlayers", is(jsonInt(2)))
            .where("team", is(jsonInt(1)))
            .where("mapPosition", is(jsonInt(3)))
            .where("commandLineArguments", is(jsonArray(contains(jsonText("/foo"), jsonText("/bar")))))
        ))));
  }

  @Test
  public void startGameProcessWithMap() {
    String response = instance.transform(new StartGameProcessResponse("faf", 1, "scmp01",
      LobbyMode.DEFAULT, null, "someName", null, 0, null, Arrays.asList("/foo", "/bar")));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("startGameProcess")))
        .where("data", is(
          jsonObject()
            .where("mod", is(jsonText("faf")))
            .where("gameId", is(jsonInt(1)))
            .where("lobbyMode", is(jsonText("DEFAULT")))
            .where("map", is(jsonText("scmp01")))
            .where("name", is(jsonText("someName")))
            .where("team", is(jsonInt(0)))
            .where("commandLineArguments", is(jsonArray(contains(jsonText("/foo"), jsonText("/bar")))))
        ))));
  }

  @Test
  public void updatedAchievements() {
    String response = instance.transform(new UpdatedAchievementsResponse(Arrays.asList(
      new UpdatedAchievement("111", 2, AchievementState.REVEALED, false),
      new UpdatedAchievement("111", 2, AchievementState.REVEALED, false)
    )));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("updatedAchievements")))
        .where("data", is(
          jsonObject()
            .where("updatedAchievements", is(jsonArray(contains(
              jsonObject()
                .where("achievementId", is(jsonText("111")))
                .where("currentSteps", is(jsonInt(2)))
                .where("currentState", is(jsonText("REVEALED")))
                .where("newlyUnlocked", is(jsonBoolean(false))),
              jsonObject()
                .where("achievementId", is(jsonText("111")))
                .where("currentSteps", is(jsonInt(2)))
                .where("currentState", is(jsonText("REVEALED")))
                .where("newlyUnlocked", is(jsonBoolean(false)))
            ))))
        ))));
  }

  @Test
  public void player() {
    String response = instance.transform(new PlayerResponse(
      1,
      "A",
      "CH",
      TimeZone.getTimeZone("Europe/Berlin"),
      new Rating(900, 120),
      new Rating(600, 50),
      12,
      new Avatar("http://localhost/avatar.png", "Avatar"),
      "AA"
    ));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("player")))
        .where("data", is(
          jsonObject()
            .where("playerId", is(jsonInt(1)))
            .where("username", is(jsonText("A")))
            .where("country", is(jsonText("CH")))
            .where("timeZone", is(jsonText("Europe/Berlin")))
            .where("globalRating", is(
              jsonObject()
                .where("mean", is(jsonDouble(900.0)))
                .where("deviation", is(jsonDouble(120.0)))
            ))
            .where("ladder1v1Rating", is(
              jsonObject()
                .where("mean", is(jsonDouble(600.0)))
                .where("deviation", is(jsonDouble(50.0)))
            ))
            .where("numberOfGames", is(jsonInt(12)))
            .where("avatar", is(
              jsonObject()
                .where("url", is(jsonText("http://localhost/avatar.png")))
                .where("description", is(jsonText("Avatar")))
            ))
            .where("clanTag", is(jsonText("AA")))
        ))));
  }

  @Test
  public void errorMessage() {
    UUID requestId = UUID.fromString("fa41225f-d818-495f-8843-d421949d5968");
    String response = instance.transform(new ErrorResponse(ErrorCode.UNSUPPORTED_REQUEST, requestId, new String[]{"{\"foo\": \"bar\"}", "JUnit Test"}));
    assertThat(response, isJsonStringMatching(
      jsonObject()
        .where("type", is(jsonText("error")))
        .where("data", is(
          jsonObject()
            .where("code", is(jsonInt(109)))
            .where("title", is(jsonText("Unsupported request")))
            .where("text", is(jsonText("The server received an unsupported request from your client: {\"foo\": \"bar\"}. Cause: JUnit Test")))
            .where("requestId", is(jsonText("fa41225f-d818-495f-8843-d421949d5968")))
            .where("args", is(jsonArray(contains(
              jsonText("{\"foo\": \"bar\"}"),
              jsonText("JUnit Test")
            ))))
        ))));
  }
}
