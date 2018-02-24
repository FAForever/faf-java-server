package com.faforever.server.integration.v2.client;

import com.faforever.server.avatar.ListAvatarsRequest;
import com.faforever.server.avatar.SelectAvatarRequest;
import com.faforever.server.client.BroadcastRequest;
import com.faforever.server.client.LoginRequest;
import com.faforever.server.common.ClientMessage;
import com.faforever.server.config.JacksonConfig;
import com.faforever.server.coop.CoopMissionCompletedReport;
import com.faforever.server.game.AiOptionReport;
import com.faforever.server.game.ArmyOutcomeReport;
import com.faforever.server.game.ArmyScoreReport;
import com.faforever.server.game.ClearSlotRequest;
import com.faforever.server.game.DesyncReport;
import com.faforever.server.game.DisconnectPeerRequest;
import com.faforever.server.game.Faction;
import com.faforever.server.game.GameChatMessageReport;
import com.faforever.server.game.GameModsReport;
import com.faforever.server.game.GameOptionReport;
import com.faforever.server.game.GameStateReport;
import com.faforever.server.game.GameVisibility;
import com.faforever.server.game.HostGameRequest;
import com.faforever.server.game.JoinGameRequest;
import com.faforever.server.game.MutuallyAgreedDrawRequest;
import com.faforever.server.game.Outcome;
import com.faforever.server.game.PlayerDefeatedReport;
import com.faforever.server.game.PlayerGameState;
import com.faforever.server.game.PlayerOptionReport;
import com.faforever.server.game.TeamKillReport;
import com.faforever.server.ice.IceMessage;
import com.faforever.server.ice.IceServersRequest;
import com.faforever.server.integration.legacy.transformer.RestoreGameSessionRequest;
import com.faforever.server.matchmaker.MatchMakerCancelRequest;
import com.faforever.server.matchmaker.MatchMakerSearchRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Duration;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class V2ClientMessageTransformerTest {

  private V2ClientMessageTransformer instance;

  private ObjectMapper objectMapper;

  @Before
  public void setUp() throws Exception {
    objectMapper = new JacksonConfig().objectMapper();

    instance = new V2ClientMessageTransformer(objectMapper, Mappers.getMapper(V2ClientMessageMapper.class));
  }

  private String write(V2ClientMessage message) throws JsonProcessingException {
    return objectMapper.writeValueAsString(new V2ClientMessageWrapper(message));
  }

  @Test
  public void agreeDraw() throws Exception {
    ClientMessage result = instance.transform(write(new AgreeDrawClientMessage()));
    assertThat(result, is(MutuallyAgreedDrawRequest.INSTANCE));
  }

  @Test
  public void aiOption() throws Exception {
    ClientMessage result = instance.transform(write(new AiOptionClientMessage("junit", "team", "1")));
    assertThat(result, is(new AiOptionReport("junit", "team", "1")));
  }

  @Test
  public void armyOutcome() throws Exception {
    ClientMessage result = instance.transform(write(new ArmyOutcomeClientMessage(1, Outcome.DEFEAT)));
    assertThat(result, is(ArmyOutcomeReport.valueOf(1, Outcome.DEFEAT)));
  }

  @Test
  public void armyScore() throws Exception {
    ClientMessage result = instance.transform(write(new ArmyScoreClientMessage(1, 10)));
    assertThat(result, is(new ArmyScoreReport(1, 10)));
  }

  @Test
  public void broadcast() throws Exception {
    ClientMessage result = instance.transform(write(new BroadcastClientMessage("Hello JUnit")));
    assertThat(result, is(new BroadcastRequest("Hello JUnit")));
  }

  @Test
  public void cancelMatchSearch() throws Exception {
    ClientMessage result = instance.transform(write(new CancelMatchSearchClientMessage("ladder1v1")));
    assertThat(result, is(new MatchMakerCancelRequest("ladder1v1")));
  }

  @Test
  public void clearSlot() throws Exception {
    ClientMessage result = instance.transform(write(new ClearSlotClientMessage(1)));
    assertThat(result, is(ClearSlotRequest.valueOf(1)));
  }

  @Test
  public void coopMissionCompleted() throws Exception {
    ClientMessage result = instance.transform(write(new CoopMissionCompletedClientMessage(true, false, 300)));
    assertThat(result, is(new CoopMissionCompletedReport(true, false, Duration.ofSeconds(300))));
  }

  @Test
  public void disconnectPeer() throws Exception {
    ClientMessage result = instance.transform(write(new DisconnectPeerClientMessage(42)));
    assertThat(result, is(new DisconnectPeerRequest(42)));
  }

  @Test
  public void gameDesync() throws Exception {
    ClientMessage result = instance.transform(write(new GameDesyncClientMessage()));
    assertThat(result, is(DesyncReport.INSTANCE));
  }

  @Test
  public void gameMods() throws Exception {
    ClientMessage result = instance.transform(write(new GameModsClientMessage(Arrays.asList("1", "2"))));
    assertThat(result, is(new GameModsReport(Arrays.asList("1", "2"))));
  }

  @Test
  public void gameOption() throws Exception {
    ClientMessage result = instance.transform(write(new GameOptionClientMessage("key", "value")));
    assertThat(result, is(new GameOptionReport("key", "value")));
  }

  @Test
  public void gameState() throws Exception {
    ClientMessage result = instance.transform(write(new GameStateClientMessage(PlayerGameState.LAUNCHING)));
    assertThat(result, is(new GameStateReport(PlayerGameState.LAUNCHING)));
  }

  @Test
  public void hostGame() throws Exception {
    ClientMessage result = instance.transform(write(new HostGameClientMessage("scmp1", "Game", "faf", "123", GameVisibility.PUBLIC, 500, 900)));
    assertThat(result, is(new HostGameRequest("scmp1", "Game", "faf", "123", GameVisibility.PUBLIC, 500, 900)));
  }

  @Test
  public void iceClient() throws Exception {
    ClientMessage result = instance.transform(write(new IceClientMessage(12, "foobar")));
    assertThat(result, is(new IceMessage(12, "foobar")));
  }

  @Test
  public void joinGame() throws Exception {
    ClientMessage result = instance.transform(write(new JoinGameClientMessage(34, "foobar")));
    assertThat(result, is(new JoinGameRequest(34, "foobar")));
  }

  @Test
  public void listAvatars() throws Exception {
    ClientMessage result = instance.transform(write(new ListAvatarsClientMessage()));
    assertThat(result, is(ListAvatarsRequest.INSTANCE));
  }

  @Test
  public void listIceServers() throws Exception {
    ClientMessage result = instance.transform(write(new ListIceServersClientMessage()));
    assertThat(result, is(IceServersRequest.INSTANCE));
  }

  @Test
  public void login() throws Exception {
    ClientMessage result = instance.transform(write(new LoginClientMessage("jwt", "123")));
    assertThat(result, is(new LoginRequest("123", "jwt")));
  }

  @Test
  public void playerDefeated() throws Exception {
    ClientMessage result = instance.transform(write(new PlayerDefeatedClientMessage()));
    assertThat(result, is(PlayerDefeatedReport.INSTANCE));
  }

  @Test
  public void playerOption() throws Exception {
    ClientMessage result = instance.transform(write(new PlayerOptionClientMessage(123, "key", "value")));
    assertThat(result, is(new PlayerOptionReport(123, "key", "value")));
  }

  @Test
  public void restoreGameSession() throws Exception {
    ClientMessage result = instance.transform(write(new RestoreGameSessionClientMessage(123)));
    assertThat(result, is(new RestoreGameSessionRequest(123)));
  }

  @Test
  public void searchMatch() throws Exception {
    ClientMessage result = instance.transform(write(new SearchMatchClientMessage(Faction.UEF, "ladder1v1")));
    assertThat(result, is(new MatchMakerSearchRequest(Faction.UEF, "ladder1v1")));
  }

  @Test
  public void selectAvatar() throws Exception {
    ClientMessage result = instance.transform(write(new SelectAvatarClientMessage(1231)));
    assertThat(result, is(new SelectAvatarRequest(1231, null)));
  }

  @Test
  public void teamKill() throws Exception {
    ClientMessage result = instance.transform(write(new TeamKillClientMessage(100, 1, "A", 2, "B")));
    assertThat(result, is(new TeamKillReport(Duration.ofSeconds(100), 1, "A", 2, "B")));
  }

  @Test
  public void gameChatMessage() throws Exception {
    ClientMessage result = instance.transform(write(new GameChatMessageClientMessage("Message")));
    assertThat(result, is(new GameChatMessageReport("Message")));
  }
}
