package com.faforever.server.matchmaker;

import com.faforever.server.client.ClientService;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.Ladder1v1Rating;
import com.faforever.server.entity.MapVersion;
import com.faforever.server.entity.MatchMakerBanDetails;
import com.faforever.server.entity.Player;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.game.Faction;
import com.faforever.server.game.GameService;
import com.faforever.server.game.GameVisibility;
import com.faforever.server.map.MapService;
import com.faforever.server.mod.ModService;
import com.faforever.server.player.PlayerService;
import com.faforever.server.rating.RatingService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.faforever.server.error.RequestExceptionWithCode.requestExceptionWithCode;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MatchMakerServiceTest {

  private static final String QUEUE_NAME = "ladder1v1";
  private static final String LOGIN_PLAYER_1 = "Player 1";
  private static final String LOGIN_PLAYER_2 = "Player 2";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private MatchMakerService instance;
  private ServerProperties properties;
  @Mock
  private ModService modService;
  @Mock
  private ClientService clientService;
  @Mock
  private GameService gameService;
  @Mock
  private MapService mapService;
  @Mock
  private PlayerService playerService;

  @Before
  public void setUp() throws Exception {
    properties = new ServerProperties();
    FeaturedMod ladder1v1Mod = new FeaturedMod().setTechnicalName("faf");

    when(modService.getLadder1v1()).thenReturn(Optional.of(ladder1v1Mod));
    when(mapService.getRandomLadderMap(any(), any())).thenReturn(new MapVersion().setFilename("maps/SCMP_001.zip"));

    RatingService ratingService = new RatingService(properties);
    instance = new MatchMakerService(modService, properties, ratingService, clientService, gameService, mapService, playerService);
  }

  @Test
  public void startSearchAlreadyInGame() {
    Player player = new Player();
    player.setCurrentGame(new Game());

    expectedException.expect(requestExceptionWithCode(ErrorCode.ALREADY_IN_GAME));
    instance.submitSearch(player, Faction.CYBRAN, QUEUE_NAME);
  }

  @Test
  public void startSearchBanned() {
    Player player = new Player();
    player.setMatchMakerBanDetails(new MatchMakerBanDetails());

    expectedException.expect(requestExceptionWithCode(ErrorCode.BANNED_FROM_MATCH_MAKER));
    instance.submitSearch(player, Faction.CYBRAN, QUEUE_NAME);
  }

  @Test
  public void startSearchModNotAvailable() {
    when(modService.getLadder1v1()).thenReturn(Optional.empty());

    expectedException.expect(requestExceptionWithCode(ErrorCode.MATCH_MAKER_POOL_DOESNT_EXIST));
    instance.submitSearch(new Player(), Faction.CYBRAN, QUEUE_NAME);
  }

  @Test
  public void startSearchEmptyQueue() {
    instance.submitSearch(new Player(), Faction.CYBRAN, QUEUE_NAME);
    instance.processPools();

    verifyZeroInteractions(gameService);
  }

  /**
   * Tests whether two players who never played a game (and thus have no rating associated) don't match immediately,
   * because such players always have a low game quality.
   */
  @Test
  public void submitSearchTwoFreshPlayersDontMatchImmediately() {
    Player player1 = (Player) new Player().setLogin(LOGIN_PLAYER_1).setId(1);
    Player player2 = (Player) new Player().setLogin(LOGIN_PLAYER_2).setId(2);

    properties.getMatchMaker().setAcceptableQualityWaitTime(10);
    instance.submitSearch(player1, Faction.CYBRAN, QUEUE_NAME);
    instance.submitSearch(player2, Faction.AEON, QUEUE_NAME);
    instance.processPools();

    verify(gameService, never()).createGame(any(), any(), any(), any(), any(), anyInt(), anyInt(), any());
    verify(gameService, never()).joinGame(anyInt(), eq(null), any());
  }

  /**
   * Tests whether two players who never played a game (and thus have no rating associated) will be matched.
   */
  @Test
  public void submitSearchTwoFreshPlayersMatch() {
    Player player1 = (Player) new Player().setLogin(LOGIN_PLAYER_1).setId(1);
    Player player2 = (Player) new Player().setLogin(LOGIN_PLAYER_2).setId(2);
    when(gameService.createGame(any(), any(), any(), any(), any(), any(), any(), any()))
      .thenReturn(CompletableFuture.completedFuture(new Game(1)));
    when(gameService.joinGame(1, null, player2))
      .thenReturn(CompletableFuture.completedFuture(new Game(1)));

    properties.getMatchMaker().setAcceptableQualityWaitTime(0);
    instance.submitSearch(player1, Faction.CYBRAN, QUEUE_NAME);
    instance.submitSearch(player2, Faction.AEON, QUEUE_NAME);
    instance.processPools();

    verify(gameService).createGame(LOGIN_PLAYER_1 + " vs. " + LOGIN_PLAYER_2, "faf", "SCMP_001",
      null, GameVisibility.PRIVATE, null, null, player1);
    verify(gameService).joinGame(1, null, player2);

    // Verify only the last player option being set, to verify it went through until the end.
    verify(gameService).updatePlayerOption(player1, player2.getId(), GameService.OPTION_ARMY, 3);
  }

  /**
   * Tests whether two players whose ratings are too far apart don't get matched.
   */
  @Test
  public void submitSearchTwoPlayersDontMatchIfRatingsTooFarApart() {
    Player player1 = (Player) new Player()
      .setLadder1v1Rating((Ladder1v1Rating) new Ladder1v1Rating().setMean(300d).setDeviation(50d))
      .setLogin(LOGIN_PLAYER_1)
      .setId(1);
    Player player2 = (Player) new Player()
      .setLadder1v1Rating((Ladder1v1Rating) new Ladder1v1Rating().setMean(1300d).setDeviation(50d))
      .setLogin(LOGIN_PLAYER_2)
      .setId(2);

    instance.submitSearch(player1, Faction.CYBRAN, QUEUE_NAME);
    instance.submitSearch(player2, Faction.AEON, QUEUE_NAME);
    instance.processPools();

    verify(gameService, never()).createGame(any(), any(), any(), any(), any(), anyInt(), anyInt(), any());
    verify(gameService, never()).joinGame(anyInt(), eq(null), any());
  }

  @Test
  public void cancelSearch() {
    Player player1 = (Player) new Player().setLogin(LOGIN_PLAYER_1).setId(1);
    Player player2 = (Player) new Player().setLogin(LOGIN_PLAYER_2).setId(2);

    instance.submitSearch(player1, Faction.CYBRAN, QUEUE_NAME);
    instance.submitSearch(player2, Faction.AEON, QUEUE_NAME);

    assertThat(instance.getSearchPools().get(QUEUE_NAME).keySet(), hasSize(2));

    instance.cancelSearch(QUEUE_NAME, player1);
    assertThat(instance.getSearchPools().get(QUEUE_NAME).keySet(), hasSize(1));

    instance.cancelSearch(QUEUE_NAME, player1);
    assertThat(instance.getSearchPools().get(QUEUE_NAME).keySet(), hasSize(1));

    instance.cancelSearch(QUEUE_NAME, player2);
    assertThat(instance.getSearchPools().get(QUEUE_NAME).keySet(), hasSize(0));
  }

  @Test
  public void onClientDisconnect() {
    Player player1 = (Player) new Player().setLogin(LOGIN_PLAYER_1).setId(1);
    Player player2 = (Player) new Player().setLogin(LOGIN_PLAYER_2).setId(2);

    instance.submitSearch(player1, Faction.CYBRAN, QUEUE_NAME);
    instance.submitSearch(player2, Faction.AEON, QUEUE_NAME);

    assertThat(instance.getSearchPools().get(QUEUE_NAME).keySet(), hasSize(2));

    instance.removePlayer(player1);

    assertThat(instance.getSearchPools().get(QUEUE_NAME).keySet(), hasSize(1));
  }

  @Test
  public void createMatch() {
    MatchParticipant participant1 = new MatchParticipant().setId(1).setFaction(Faction.UEF).setTeam(1).setSlot(1).setStartSpot(1);
    MatchParticipant participant2 = new MatchParticipant().setId(2).setFaction(Faction.AEON).setTeam(2).setSlot(2).setStartSpot(2);

    ConnectionAware requester = mock(ConnectionAware.class);
    UUID requestId = UUID.randomUUID();
    List<MatchParticipant> participants = Arrays.asList(
      participant1, participant2
    );
    int mapVersionId = 1;

    Player player1 = (Player) new Player().setId(1);
    Player player2 = (Player) new Player().setId(2);

    when(playerService.getOnlinePlayer(participant1.getId())).thenReturn(Optional.of(player1));
    when(playerService.getOnlinePlayer(participant2.getId())).thenReturn(Optional.of(player2));
    when(mapService.findMap(mapVersionId)).thenReturn(Optional.of(new MapVersion().setFilename("maps/foo.zip")));

    Game game = new Game().setId(1);
    when(gameService.createGame("Test match", "faf", "foo", null, GameVisibility.PRIVATE, null, null, player1))
      .thenReturn(CompletableFuture.completedFuture(game));

    when(gameService.joinGame(1, null, player2)).thenReturn(CompletableFuture.completedFuture(game));

    instance.createMatch(requester, requestId, "Test match", "faf", participants, mapVersionId);

    verify(clientService, timeout(5000)).sendMatchCreatedNotification(requestId, 1, requester);

    verify(gameService, timeout(1000)).updatePlayerOption(player1, player1.getId(), GameService.OPTION_TEAM, participant1.getTeam());
    verify(gameService, timeout(1000)).updatePlayerOption(player1, player1.getId(), GameService.OPTION_FACTION, participant1.getFaction().toFaValue());
    verify(gameService, timeout(1000)).updatePlayerOption(player1, player1.getId(), GameService.OPTION_START_SPOT, participant1.getStartSpot());
    verify(gameService, timeout(1000)).updatePlayerOption(player1, player1.getId(), GameService.OPTION_COLOR, 1);
    verify(gameService, timeout(1000)).updatePlayerOption(player1, player1.getId(), GameService.OPTION_ARMY, 1);

    verify(gameService, timeout(1000)).updatePlayerOption(player1, player2.getId(), GameService.OPTION_TEAM, participant2.getTeam());
    verify(gameService, timeout(1000)).updatePlayerOption(player1, player2.getId(), GameService.OPTION_FACTION, participant2.getFaction().toFaValue());
    verify(gameService, timeout(1000)).updatePlayerOption(player1, player2.getId(), GameService.OPTION_START_SPOT, participant2.getStartSpot());
    verify(gameService, timeout(1000)).updatePlayerOption(player1, player2.getId(), GameService.OPTION_COLOR, 2);
    verify(gameService, timeout(1000)).updatePlayerOption(player1, player2.getId(), GameService.OPTION_ARMY, 2);
  }

  // TODO test updating queue
}
