package com.faforever.server.matchmaker;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.client.ClientService;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.Ladder1v1Rating;
import com.faforever.server.entity.MapVersion;
import com.faforever.server.entity.MatchMakerBanDetails;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.game.Faction;
import com.faforever.server.game.GameService;
import com.faforever.server.game.GameVisibility;
import com.faforever.server.integration.Protocol;
import com.faforever.server.map.MapService;
import com.faforever.server.mod.ModService;
import com.faforever.server.player.PlayerService;
import com.faforever.server.rating.RatingService;
import com.faforever.server.security.FafUserDetails;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.faforever.server.error.RequestExceptionWithCode.requestExceptionWithCode;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
  private RatingService ratingService;
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

  private FeaturedMod ladder1v1Mod;

  @Before
  public void setUp() throws Exception {
    properties = new ServerProperties();
    ladder1v1Mod = new FeaturedMod().setId(1);

    when(modService.getFeaturedMod(ladder1v1Mod.getId())).thenReturn(Optional.of(ladder1v1Mod));
    when(modService.getLadder1v1()).thenReturn(ladder1v1Mod);
    when(modService.isLadder1v1(ladder1v1Mod)).thenReturn(true);
    when(mapService.getRandomLadderMap()).thenReturn(new MapVersion().setFilename("SCMP_001"));
    when(gameService.createGame(any(), anyInt(), any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(new Game()));

    ratingService = new RatingService(properties);
    instance = new MatchMakerService(modService, properties, ratingService, clientService, gameService, mapService, playerService);
    instance.postConstruct();
  }

  @Test
  public void startSearchAlreadyInGame() throws Exception {
    Player player = new Player();
    player.setCurrentGame(new Game());

    expectedException.expect(requestExceptionWithCode(ErrorCode.ALREADY_IN_GAME));
    instance.submitSearch(player, Faction.CYBRAN, QUEUE_NAME);
  }

  @Test
  public void startSearchBanned() throws Exception {
    Player player = new Player();
    player.setMatchMakerBanDetails(new MatchMakerBanDetails());

    expectedException.expect(requestExceptionWithCode(ErrorCode.BANNED_FROM_MATCH_MAKER));
    instance.submitSearch(player, Faction.CYBRAN, QUEUE_NAME);
  }

  @Test
  public void startSearchModNotAvailable() throws Exception {
    when(modService.getLadder1v1()).thenReturn(null);
    instance.postConstruct();

    expectedException.expect(requestExceptionWithCode(ErrorCode.MATCHMAKER_1V1_ONLY));
    instance.submitSearch(new Player(), Faction.CYBRAN, QUEUE_NAME);
  }

  @Test
  public void startSearchEmptyQueue() throws Exception {
    FeaturedMod featuredMod = new FeaturedMod();
    when(modService.getFeaturedMod(1)).thenReturn(Optional.of(featuredMod));
    when(modService.isLadder1v1(featuredMod)).thenReturn(true);

    instance.submitSearch(new Player(), Faction.CYBRAN, QUEUE_NAME);
    instance.processPools();

    verifyZeroInteractions(gameService);
  }

  /**
   * Tests whether two players who never played a game (and thus have no rating associated) don't match immediately,
   * because such players always have a low game quality.
   */
  @Test
  public void submitSearchTwoFreshPlayersDontMatchImmediately() throws Exception {
    Player player1 = (Player) new Player().setLogin(LOGIN_PLAYER_1).setId(1);
    Player player2 = (Player) new Player().setLogin(LOGIN_PLAYER_2).setId(2);

    properties.getMatchMaker().setAcceptableQualityWaitTime(10);
    instance.submitSearch(player1, Faction.CYBRAN, QUEUE_NAME);
    instance.submitSearch(player2, Faction.AEON, QUEUE_NAME);
    instance.processPools();

    verify(gameService, never()).createGame(any(), anyInt(), any(), any(), any(), any());
    verify(gameService, never()).joinGame(anyInt(), any());
  }

  /**
   * Tests whether two players who never played a game (and thus have no rating associated) will be matched.
   */
  @Test
  public void submitSearchTwoFreshPlayersMatch() throws Exception {
    Player player1 = (Player) new Player().setLogin(LOGIN_PLAYER_1).setId(1);
    Player player2 = (Player) new Player().setLogin(LOGIN_PLAYER_2).setId(2);

    properties.getMatchMaker().setAcceptableQualityWaitTime(0);
    instance.submitSearch(player1, Faction.CYBRAN, QUEUE_NAME);
    instance.submitSearch(player2, Faction.AEON, QUEUE_NAME);
    instance.processPools();

    verify(gameService).createGame(LOGIN_PLAYER_1 + " vs. " + LOGIN_PLAYER_2, 1, "SCMP_001",
      null, GameVisibility.PRIVATE, player1);
    verify(gameService).joinGame(0, player2);
  }

  /**
   * Tests whether two players whose ratings are too far apart don't get matched.
   */
  @Test
  public void submitSearchTwoPlayersDontMatchIfRatingsTooFarApart() throws Exception {
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

    verify(gameService, never()).createGame(any(), anyInt(), any(), any(), any(), any());
    verify(gameService, never()).joinGame(anyInt(), any());
  }

  @Test
  public void cancelSearch() throws Exception {
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
  public void onClientDisconnect() throws Exception {
    Player player1 = (Player) new Player().setLogin(LOGIN_PLAYER_1).setId(1);
    Player player2 = (Player) new Player().setLogin(LOGIN_PLAYER_2).setId(2);

    instance.submitSearch(player1, Faction.CYBRAN, QUEUE_NAME);
    instance.submitSearch(player2, Faction.AEON, QUEUE_NAME);

    assertThat(instance.getSearchPools().get(QUEUE_NAME).keySet(), hasSize(2));

    ClientConnection clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class))
      .setUserDetails(new FafUserDetails((User) new User()
        .setPlayer(player1)
        .setPassword("p")
        .setLogin(player1.getLogin())));

    instance.onClientDisconnect(new ClientDisconnectedEvent(this, clientConnection));

    assertThat(instance.getSearchPools().get(QUEUE_NAME).keySet(), hasSize(1));
  }

  // TODO test updating queue
}
