package com.faforever.server.client;

import com.faforever.server.api.dto.AchievementState;
import com.faforever.server.api.dto.UpdatedAchievement;
import com.faforever.server.coop.CoopService;
import com.faforever.server.entity.Avatar;
import com.faforever.server.entity.AvatarAssociation;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.Ladder1v1Rating;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.game.HostGameResponse;
import com.faforever.server.integration.ClientGateway;
import com.faforever.server.integration.Protocol;
import com.faforever.server.integration.response.StartGameProcessResponse;
import com.faforever.server.mod.FeaturedModResponse;
import com.faforever.server.player.PlayerService;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.UserDetailsResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientServiceTest {

  private ClientService instance;

  @Mock
  private ClientGateway clientGateway;
  @Mock
  private CoopService coopService;
  @Mock
  private PlayerService playerService;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  private ClientConnection clientConnection;
  private Player player;

  @Before
  public void setUp() throws Exception {
    clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16);
    player = new Player().setClientConnection(clientConnection);

    instance = new ClientService(clientGateway, coopService, playerService, eventPublisher);
  }

  @Test
  public void connectToPlayer() throws Exception {
    Player peer = new Player();
    peer.setId(2);
    peer.setLogin("test");

    instance.connectToPlayer(player, peer);

    ArgumentCaptor<ConnectToPlayerResponse> captor = ArgumentCaptor.forClass(ConnectToPlayerResponse.class);
    verify(clientGateway).send(captor.capture(), eq(clientConnection));
    ConnectToPlayerResponse response = captor.getValue();

    assertThat(response.getPlayerId(), is(peer.getId()));
    assertThat(response.getPlayerName(), is(peer.getLogin()));
  }

  @Test
  public void connectToHost() throws Exception {
    Player host = new Player();
    host.setId(1);

    Game game = new Game();
    game.setHost(host);

    instance.connectToHost(game, player);

    ArgumentCaptor<ConnectToHostResponse> captor = ArgumentCaptor.forClass(ConnectToHostResponse.class);
    verify(clientGateway).send(captor.capture(), eq(clientConnection));
    ConnectToHostResponse response = captor.getValue();

    assertThat(response.getHostId(), is(host.getId()));
  }

  @Test
  public void startGameProcess() throws Exception {
    Game game = new Game().setId(1).setFeaturedMod(new FeaturedMod());

    instance.startGameProcess(game, player);

    ArgumentCaptor<StartGameProcessResponse> captor = ArgumentCaptor.forClass(StartGameProcessResponse.class);
    verify(clientGateway).send(captor.capture(), any());

    assertThat(captor.getValue().getGameId(), is(1));
  }

  @Test
  public void hostGame() throws Exception {
    Game game = new Game().setId(1).setMapName("SCMP_001");

    instance.hostGame(game, player);

    ArgumentCaptor<HostGameResponse> captor = ArgumentCaptor.forClass(HostGameResponse.class);
    verify(clientGateway).send(captor.capture(), any());

    assertThat(captor.getValue().getMapFilename(), is("SCMP_001"));
  }

  @Test
  public void reportUpdatedAchievements() throws Exception {
    List<UpdatedAchievement> list = Collections.singletonList(new UpdatedAchievement(true, AchievementState.UNLOCKED));

    instance.reportUpdatedAchievements(list, player);

    ArgumentCaptor<UpdatedAchievementsResponse> captor = ArgumentCaptor.forClass(UpdatedAchievementsResponse.class);
    verify(clientGateway).send(captor.capture(), any());

    assertThat(captor.getValue().getUpdatedAchievements(), hasSize(1));
    assertThat(captor.getValue().getUpdatedAchievements().get(0).getState(), is(AchievementState.UNLOCKED));
    assertThat(captor.getValue().getUpdatedAchievements().get(0).getCurrentSteps(), is(nullValue()));
  }

  @Test
  public void sendUserDetails() throws Exception {
    Avatar avatar = new Avatar().setUrl("http://example.com").setTooltip("Tooltip");
    player
      .setAvailableAvatars(Collections.singletonList(
        new AvatarAssociation().setAvatar(avatar).setPlayer(player).setSelected(true)
      ))
      .setGlobalRating((GlobalRating) new GlobalRating().setNumGames(12).setMean(1100d).setDeviation(100d))
      .setLadder1v1Rating((Ladder1v1Rating) new Ladder1v1Rating().setMean(900d).setDeviation(50d))
      .setCountry("CH")
      .setId(5);

    User user = (User) new User()
      .setPlayer(player)
      .setPassword("")
      .setLogin("JUnit")
      .setCountry(player.getCountry())
      .setId(player.getId());

    FafUserDetails fafUserDetails = new FafUserDetails(user);

    instance.sendUserDetails(fafUserDetails, player);

    ArgumentCaptor<UserDetailsResponse> captor = ArgumentCaptor.forClass(UserDetailsResponse.class);
    verify(clientGateway).send(captor.capture(), any());

    UserDetailsResponse response = captor.getValue();
    assertThat(response.getUserId(), is(5));
    assertThat(response.getUsername(), is("JUnit"));
    assertThat(response.getPlayer().getAvatar().getTooltip(), is("Tooltip"));
    assertThat(response.getPlayer().getAvatar().getUrl(), is("http://example.com"));
    assertThat(response.getPlayer().getGlobalRating().getMean(), is(1100d));
    assertThat(response.getPlayer().getGlobalRating().getDeviation(), is(100d));
    assertThat(response.getPlayer().getLadder1v1Rating().getMean(), is(900d));
    assertThat(response.getPlayer().getLadder1v1Rating().getDeviation(), is(50d));
    assertThat(response.getPlayer().getNumberOfGames(), is(12));
    assertThat(response.getCountry(), is("CH"));
  }

  @Test
  public void sendModList() throws Exception {
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
  public void sendModListSendsMultiple() throws Exception {
    instance.sendModList(Arrays.asList(new FeaturedMod(), new FeaturedMod()), player);
    verify(clientGateway, times(2)).send(any(FeaturedModResponse.class), eq(clientConnection));
  }

  @Test
  public void disconnectPlayerSendsToAllPlayersInGame() throws Exception {
    List<Player> recipients = Arrays.asList(player, new Player(), new Player(), new Player());

    instance.disconnectPlayer(12, recipients);

    verify(clientGateway, times(4)).send(any(DisconnectPlayerResponse.class), any());
  }

  @Test
  public void disconnectClient() throws Exception {
    ClientConnection clientConnection12 = new ClientConnection("1", Protocol.LEGACY_UTF_16);
    Player player12 = new Player()
      .setClientConnection(clientConnection12);
    when(playerService.getPlayer(12)).thenReturn(Optional.of(player12));

    instance.disconnectClient(new User(), 12);

    ArgumentCaptor<CloseConnectionEvent> captor = ArgumentCaptor.forClass(CloseConnectionEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    CloseConnectionEvent value = captor.getValue();

    assertThat(value.getClientConnection(), is(clientConnection12));
  }
}
