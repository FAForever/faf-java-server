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
import com.faforever.server.entity.GameState;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.ErrorResponse;
import com.faforever.server.game.GameResponse;
import com.faforever.server.game.GameResponse.FeaturedModFileVersion;
import com.faforever.server.game.GameResponse.Player;
import com.faforever.server.game.GameResponse.SimMod;
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
import com.faforever.server.player.PlayerResponse.Player.Avatar;
import com.faforever.server.player.PlayerResponse.Player.Rating;
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
    assertThat(response, is("{\"data\":{\"message\":\"Hello JUnit\"},\"type\":\"info\"}"));
  }

  @Test
  public void hostGame() {
    String response = instance.transform(new HostGameResponse("scmp01"));
    assertThat(response, is("{\"data\":{\"mapName\":\"scmp01\"},\"type\":\"hostGame\"}"));
  }

  @Test
  public void joinChatChannel() {
    String response = instance.transform(new JoinChatChannelResponse(ImmutableSet.of("#one", "#two")));
    assertThat(response, is("{\"data\":{\"channels\":[\"#one\",\"#two\"]},\"type\":\"chatChannel\"}"));
  }

  @Test
  public void connectToPeer() {
    String response = instance.transform(new ConnectToPeerResponse("junit", 123, false));
    assertThat(response, is("{\"data\":{\"playerName\":\"junit\",\"playerId\":123},\"type\":\"connectToPeer\"}"));
  }

  @Test
  public void disconnectFromPlayer() {
    String response = instance.transform(new DisconnectPlayerFromGameResponse(123));
    assertThat(response, is("{\"data\":{\"playerId\":123},\"type\":\"disconnectFromPeer\"}"));
  }

  @Test
  public void featuredMod() {
    String response = instance.transform(new FeaturedModResponse("faf", "Forged Alliance Forever", "Description", 1));
    assertThat(response, is("{\"data\":{\"technicalName\":\"faf\",\"displayName\":\"Forged Alliance Forever\",\"description\":\"Description\",\"displayOrder\":1},\"type\":\"featuredMod\"}"));
  }

  @Test
  public void game() {
    Instant startTime = Instant.parse("2007-12-03T10:15:30.00Z");

    String response = instance.transform(new GameResponse(1, "Title", GameVisibility.PUBLIC, "password", GameState.ENDED, "faf",
        Arrays.asList(
          new SimMod("1-1-1-1", "Mod #1"),
          new SimMod("2-2-2-2", "Mod #2")
        ),
        "scmp01", "JUnit4",
        Arrays.asList(
          new Player(1, "JUnit4"),
          new Player(2, "JUnit5")
        ),
        12,
        startTime,
        500,
        800,
        Arrays.asList(
          new FeaturedModFileVersion((short) 1, 4444),
          new FeaturedModFileVersion((short) 2, 6789)
        )
      )
    );
    assertThat(response, is("{\"data\":{\"id\":1,\"title\":\"Title\",\"gameVisibility\":\"PUBLIC\",\"password\":\"password\",\"state\":\"ENDED\",\"featuredModTechnicalName\":\"faf\",\"simMods\":[{\"uid\":\"1-1-1-1\",\"displayName\":\"Mod #1\"},{\"uid\":\"2-2-2-2\",\"displayName\":\"Mod #2\"}],\"technicalMapName\":\"scmp01\",\"hostUsername\":\"JUnit4\",\"players\":[{\"team\":1,\"name\":\"JUnit4\"},{\"team\":2,\"name\":\"JUnit5\"}],\"maxPlayers\":12,\"startTime\":1196676930000,\"minRating\":500,\"maxRating\":800,\"featuredModFileVersions\":[{\"id\":1,\"version\":4444},{\"id\":2,\"version\":6789}]},\"type\":\"game\"}"));
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
    assertThat(response, is("{\"data\":{\"iceServerLists\":[{\"ttlSeconds\":3600,\"createdAt\":1196676930000,\"servers\":[{\"url\":\"http://localhost\",\"username\":\"anonymous\",\"credential\":\"123\",\"credentialType\":\"token\"}]}]},\"type\":\"iceServers\"}"));
  }

  @Test
  public void loginDetails() {
    String response = instance.transform(new LoginDetailsResponse(new PlayerResponse(
      1,
      "A",
      "CH",
      TimeZone.getTimeZone("Europe/Berlin"),
      new PlayerResponse.Player(
        new Rating(900, 120),
        new Rating(600, 50),
        12,
        new Avatar("http://localhost/avatar.png", "Avatar"),
        "AA"
      ))));
    assertThat(response, is("{\"data\":{\"playerId\":1,\"username\":\"A\",\"country\":\"CH\",\"timeZone\":\"Europe/Berlin\",\"player\":{\"globalRating\":{\"mean\":900.0,\"deviation\":120.0},\"ladder1v1Rating\":{\"mean\":600.0,\"deviation\":50.0},\"numberOfGames\":12,\"avatar\":{\"url\":\"http://localhost/avatar.png\",\"description\":\"Avatar\"},\"clanTag\":\"AA\"}},\"type\":\"loginDetails\"}"));
  }

  @Test
  public void matchAvailable() {
    String response = instance.transform(new MatchMakerResponse("ladder2v2"));
    assertThat(response, is("{\"data\":{\"pool\":\"ladder2v2\"},\"type\":\"matchAvailable\"}"));
  }

  @Test
  public void socialRelationList() {
    String response = instance.transform(new SocialRelationListResponse(Arrays.asList(
      new SocialRelationResponse(1, RelationType.FRIEND),
      new SocialRelationResponse(2, RelationType.FOE)
    )));
    assertThat(response, is("{\"data\":{\"socialRelations\":[{\"playerId\":1,\"type\":\"FRIEND\"},{\"playerId\":2,\"type\":\"FOE\"}]},\"type\":\"socialRelations\"}"));
  }

  @Test
  public void startGameProcessWithoutMap() {
    String response = instance.transform(new StartGameProcessResponse("faf", 1, null, LobbyMode.DEFAULT, Arrays.asList("/foo", "/bar")));
    assertThat(response, is("{\"data\":{\"mod\":\"faf\",\"gameId\":1,\"lobbyMode\":\"DEFAULT\",\"commandLineArguments\":[\"/foo\",\"/bar\"]},\"type\":\"startGameProcess\"}"));
  }

  @Test
  public void startGameProcessWithMap() {
    String response = instance.transform(new StartGameProcessResponse("faf", 1, "scmp01", LobbyMode.DEFAULT, Arrays.asList("/foo", "/bar")));
    assertThat(response, is("{\"data\":{\"mod\":\"faf\",\"gameId\":1,\"map\":\"scmp01\",\"lobbyMode\":\"DEFAULT\",\"commandLineArguments\":[\"/foo\",\"/bar\"]},\"type\":\"startGameProcess\"}"));
  }

  @Test
  public void updatedAchievements() {
    String response = instance.transform(new UpdatedAchievementsResponse(Arrays.asList(
      new UpdatedAchievement("111", 2, AchievementState.REVEALED, false),
      new UpdatedAchievement("111", 2, AchievementState.REVEALED, false)
    )));
    assertThat(response, is("{\"data\":{\"updatedAchievements\":[{\"achievementId\":\"111\",\"currentSteps\":2,\"currentState\":\"REVEALED\",\"newlyUnlocked\":false},{\"achievementId\":\"111\",\"currentSteps\":2,\"currentState\":\"REVEALED\",\"newlyUnlocked\":false}]},\"type\":\"updatedAchievements\"}"));
  }

  @Test
  public void player() {
    String response = instance.transform(new PlayerResponse(
      1,
      "A",
      "CH",
      TimeZone.getTimeZone("Europe/Berlin"),
      new PlayerResponse.Player(
        new Rating(900, 120),
        new Rating(600, 50),
        12,
        new Avatar("http://localhost/avatar.png", "Avatar"),
        "AA"
      )));
    assertThat(response, is("{\"data\":{\"playerId\":1,\"username\":\"A\",\"country\":\"CH\",\"timeZone\":\"Europe/Berlin\",\"player\":{\"globalRating\":{\"mean\":900.0,\"deviation\":120.0},\"ladder1v1Rating\":{\"mean\":600.0,\"deviation\":50.0},\"numberOfGames\":12,\"avatar\":{\"url\":\"http://localhost/avatar.png\",\"description\":\"Avatar\"},\"clanTag\":\"AA\"}},\"type\":\"player\"}"));
  }

  @Test
  public void errorMessage() {
    UUID requestId = UUID.randomUUID();
    String response = instance.transform(new ErrorResponse(ErrorCode.UNSUPPORTED_REQUEST, requestId, new String[]{"{\"foo\": \"bar\"}"}));
    assertThat(response, is("{\"data\":{\"code\":109,\"title\":\"Unsupported request\",\"text\":\"The server received an unsupported request from your client: {\\\"foo\\\": \\\"bar\\\"}\",\"requestId\":\"" + requestId + "\",\"args\":[\"{\\\"foo\\\": \\\"bar\\\"}\"]},\"type\":\"error\"}"));
  }
}
