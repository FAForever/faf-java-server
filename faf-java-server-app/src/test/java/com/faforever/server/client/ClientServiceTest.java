package com.faforever.server.client;

import com.faforever.server.FafServerApplication.ApplicationShutdownEvent;
import com.faforever.server.api.dto.AchievementState;
import com.faforever.server.api.dto.UpdatedAchievementResponse;
import com.faforever.server.avatar.Avatar;
import com.faforever.server.clan.Clan;
import com.faforever.server.common.ServerMessage;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.coop.CoopService;
import com.faforever.server.game.Game;
import com.faforever.server.game.HostGameResponse;
import com.faforever.server.game.LobbyMode;
import com.faforever.server.game.StartGameProcessResponse;
import com.faforever.server.ice.ForwardedIceMessage;
import com.faforever.server.ice.IceServer;
import com.faforever.server.ice.IceServerList;
import com.faforever.server.integration.ClientGateway;
import com.faforever.server.integration.Protocol;
import com.faforever.server.ladder1v1.Ladder1v1Rating;
import com.faforever.server.mod.FeaturedMod;
import com.faforever.server.mod.FeaturedModResponse;
import com.faforever.server.player.Player;
import com.faforever.server.player.PlayerResponse;
import com.faforever.server.rating.GlobalRating;
import com.faforever.server.social.SocialRelationListResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.InetAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ClientServiceTest {

  private ClientService instance;

  @Mock
  private ClientGateway clientGateway;
  @Mock
  private CoopService coopService;

  @Captor
  private ArgumentCaptor<ServerMessage> serverMessageCaptor;

  private ClientConnection clientConnection;
  private Player player;
  private ServerProperties serverProperties;

  @Before
  public void setUp() throws Exception {
    serverProperties = new ServerProperties();
    clientConnection = new ClientConnection("1", Protocol.V1_LEGACY_UTF_16, mock(InetAddress.class));
    player = new Player().setClientConnection(clientConnection);
    player.setLogin("playerLogin");

    instance = new ClientService(clientGateway, coopService, serverProperties);
  }

  @Test
  public void connectToPeer() {
    Player peer = new Player();
    peer.setId(2);
    peer.setLogin("test");

    instance.connectToPeer(player, peer, false);

    ArgumentCaptor<ConnectToPeerResponse> captor = ArgumentCaptor.forClass(ConnectToPeerResponse.class);
    verify(clientGateway).send(captor.capture(), eq(clientConnection));
    ConnectToPeerResponse response = captor.getValue();

    assertThat(response.getPlayerId(), is(peer.getId()));
    assertThat(response.getPlayerName(), is(peer.getLogin()));
    assertThat(response.isOffer(), is(false));
  }

  @Test
  public void startGameProcess() {
    Game game = new Game().setId(1)
      .setFeaturedMod(new FeaturedMod().setTechnicalName("junit"))
      .setMapFolderName("scmp_001")
      .setLobbyMode(LobbyMode.DEFAULT);

    instance.startGameProcess(game, player);

    ArgumentCaptor<StartGameProcessResponse> captor = ArgumentCaptor.forClass(StartGameProcessResponse.class);
    verify(clientGateway).send(captor.capture(), any());

    StartGameProcessResponse message = captor.getValue();
    assertThat(message.getGameId(), is(1));
    assertThat(message.getLobbyMode(), is(LobbyMode.DEFAULT));
    assertThat(message.getMod(), is("junit"));
    assertThat(message.getMapFolderName(), is("scmp_001"));
  }

  @Test
  public void hostGame() {
    Game game = new Game().setId(1).setMapFolderName("SCMP_001");

    instance.hostGame(game, player);

    ArgumentCaptor<HostGameResponse> captor = ArgumentCaptor.forClass(HostGameResponse.class);
    verify(clientGateway).send(captor.capture(), any());

    assertThat(captor.getValue().getMapFilename(), is("SCMP_001"));
  }

  @Test
  public void reportUpdatedAchievements() {
    List<UpdatedAchievementResponse> list = Collections.singletonList(new UpdatedAchievementResponse("1", "1", null, AchievementState.UNLOCKED, true));

    instance.reportUpdatedAchievements(list, player);

    ArgumentCaptor<UpdatedAchievementsResponse> captor = ArgumentCaptor.forClass(UpdatedAchievementsResponse.class);
    verify(clientGateway).send(captor.capture(), any());

    assertThat(captor.getValue().getUpdatedAchievements(), hasSize(1));
    assertThat(captor.getValue().getUpdatedAchievements().get(0).getCurrentState(), is(AchievementState.UNLOCKED));
    assertThat(captor.getValue().getUpdatedAchievements().get(0).getCurrentSteps(), is(nullValue()));
  }

  @Test
  public void sendUserDetails() {
    Avatar avatar = new Avatar().setUrl("http://example.com").setDescription("Tooltip");
    player
      .setAvatar(avatar)
      .setGlobalRating((GlobalRating) new GlobalRating().setNumGames(12).setMean(1100d).setDeviation(100d))
      .setLadder1v1Rating((Ladder1v1Rating) new Ladder1v1Rating().setMean(900d).setDeviation(50d))
      .setClan(new Clan().setTag("FOO"))
      .setCountry("CH")
      .setLogin("JUnit")
      .setId(5);

    instance.sendLoginDetails(player, player);

    ArgumentCaptor<PlayerResponse> captor = ArgumentCaptor.forClass(PlayerResponse.class);
    verify(clientGateway).send(captor.capture(), any());

    PlayerResponse response = captor.getValue();
    assertThat(response.getPlayerId(), is(5));
    assertThat(response.getUsername(), is("JUnit"));
    assertThat(response.getAvatar().getDescription(), is("Tooltip"));
    assertThat(response.getAvatar().getUrl(), is("http://example.com"));
    assertThat(response.getGlobalRating().getMean(), is(1100d));
    assertThat(response.getGlobalRating().getDeviation(), is(100d));
    assertThat(response.getLadder1v1Rating().getMean(), is(900d));
    assertThat(response.getLadder1v1Rating().getDeviation(), is(50d));
    assertThat(response.getNumberOfGames(), is(12));
    assertThat(response.getClanTag(), is("FOO"));
    assertThat(response.getCountry(), is("CH"));
  }

  @Test
  public void sendModList() {
    instance.sendModList(Collections.singletonList(
      new FeaturedMod().setDisplayName("Mod").setTechnicalName("mod").setDisplayOrder(4).setDescription("Description")
    ), player);

    ArgumentCaptor<FeaturedModResponse> captor = ArgumentCaptor.forClass(FeaturedModResponse.class);
    verify(clientGateway).send(captor.capture(), eq(clientConnection));
    FeaturedModResponse response = captor.getValue();

    assertThat(response.getDisplayName(), is("Mod"));
    assertThat(response.getTechnicalName(), is("mod"));
    assertThat(response.getDisplayOrder(), is(4));
    assertThat(response.getDescription(), is("Description"));
  }

  @Test
  public void sendModListSendsMultiple() {
    instance.sendModList(Arrays.asList(new FeaturedMod(), new FeaturedMod()), player);
    verify(clientGateway, times(2)).send(any(FeaturedModResponse.class), eq(clientConnection));
  }

  @Test
  public void disconnectPlayerSendsToAllPlayersInGame() {
    List<Player> recipients = Arrays.asList(player, new Player(), new Player(), new Player());

    instance.disconnectPlayerFromGame(12, recipients);

    verify(clientGateway, times(4)).send(any(DisconnectPlayerFromGameResponse.class), any());
  }

  @Test
  public void sendOnlinePlayerList() throws Exception {
    List<Player> players = Arrays.asList(
      (Player) new Player().setId(1).setLogin("JUnit").setCountry("CH"),
      (Player) new Player().setId(2).setLogin("JUnit").setCountry("CH")
    );
    ConnectionAware connectionAware = new Player().setClientConnection(clientConnection);

    CompletableFuture<PlayerResponses> sent = new CompletableFuture<>();
    doAnswer(invocation -> sent.complete(invocation.getArgument(0)))
      .when(clientGateway).send(any(PlayerResponses.class), eq(clientConnection));

    instance.sendPlayerInformation(players, connectionAware);

    PlayerResponses responses = sent.get(10, TimeUnit.SECONDS);
    assertThat(responses.getResponses(), hasSize(2));

    Iterator<PlayerResponse> iterator = responses.getResponses().iterator();
    assertThat(iterator.next().getPlayerId(), is(1));
    assertThat(iterator.next().getPlayerId(), is(2));
  }

  @Test
  public void sendIceServers() {
    List<IceServerList> iceServers = Collections.singletonList(
      new IceServerList(60, Instant.now(), Arrays.asList(
        new IceServer(URI.create("turn:test1"), null, null, null),
        new IceServer(URI.create("turn:test2"), "username", "credential", "credentialType")
      ))
    );
    ConnectionAware connectionAware = new Player().setClientConnection(clientConnection);

    instance.sendIceServers(iceServers, connectionAware);

    verify(clientGateway).send(new IceServersResponse(iceServers), clientConnection);
  }

  @Test
  public void sendIceMessage() {
    instance.sendIceMessage(1, Collections.emptyMap(), clientConnection);

    verify(clientGateway).send(new ForwardedIceMessage(1, Collections.emptyMap()), clientConnection);
  }

  @Test
  public void sendSocialRelations() {
    SocialRelationListResponse response = new SocialRelationListResponse(emptyList());
    instance.sendSocialRelations(response, clientConnection);

    verify(clientGateway).send(response, clientConnection);
  }

  @Test
  public void onServerShutdown() {
    serverProperties.getShutdown().setMessage("Shutdown test message");
    instance.onServerShutdown(ApplicationShutdownEvent.INSTANCE);

    ArgumentCaptor<InfoResponse> captor = ArgumentCaptor.forClass(InfoResponse.class);
    verify(clientGateway).broadcast(captor.capture());

    InfoResponse value = captor.getValue();
    assertThat(value.getMessage(), is("Shutdown test message"));
  }

  @Test
  public void onServerShutdownExceptionDoesntPropagate() {
    doThrow(new RuntimeException("This exception should be logged but not thrown"))
      .when(clientGateway).broadcast(any());

    instance.onServerShutdown(ApplicationShutdownEvent.INSTANCE);

    verify(clientGateway).broadcast(any());
    // Expect no exception to be thrown
  }

  @Test
  public void sendAvatarList() {
    List<Avatar> avatars = Arrays.asList(
      new Avatar().setUrl("http://example.com/foo.bar").setDescription("Foo bar"),
      new Avatar()
    );

    ConnectionAware connectionAware = new Player().setClientConnection(clientConnection);

    instance.sendAvatarList(avatars, connectionAware);

    ArgumentCaptor<AvatarsResponse> captor = ArgumentCaptor.forClass(AvatarsResponse.class);
    verify(clientGateway).send(captor.capture(), eq(clientConnection));

    assertThat(captor.getValue().getAvatars(), hasSize(2));
    assertThat(captor.getValue().getAvatars().get(0).getUrl(), is("http://example.com/foo.bar"));
    assertThat(captor.getValue().getAvatars().get(0).getDescription(), is("Foo bar"));
  }

  @Test
  public void connectToHost() {
    Game game = new Game().setHost((Player) new Player().setLogin("JUnit").setId(1));

    instance.connectToHost(player, game);

    verify(clientGateway).send(new ConnectToHostResponse("JUnit", 1), clientConnection);
  }

  @Test
  public void broadcastPlayerInformation() {
    instance.broadcastMinDelay = Duration.ofMinutes(-1);

    instance.broadcastPlayerInformation(Arrays.asList(
      (Player) new Player().setId(1),
      (Player) new Player().setId(2),
      (Player) new Player().setId(3)
    ));
    instance.broadcastDelayedResponses();

    verify(clientGateway).broadcast(serverMessageCaptor.capture());
    ServerMessage message = serverMessageCaptor.getValue();

    assertThat(message, instanceOf(PlayerResponses.class));
  }
}
