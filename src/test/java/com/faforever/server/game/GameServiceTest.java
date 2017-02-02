package com.faforever.server.game;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.client.ClientService;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.ArmyOutcome;
import com.faforever.server.entity.ArmyScore;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.GamePlayerStats;
import com.faforever.server.entity.GameState;
import com.faforever.server.entity.MapVersion;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.Rankiness;
import com.faforever.server.entity.User;
import com.faforever.server.entity.VictoryCondition;
import com.faforever.server.integration.Protocol;
import com.faforever.server.map.MapService;
import com.faforever.server.mod.ModService;
import com.faforever.server.player.PlayerService;
import com.faforever.server.rating.RatingService;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.stats.ArmyStatistics;
import com.faforever.server.stats.ArmyStatisticsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
// TODO more testing needed
public class GameServiceTest {

  private static final String PLAYER_NAME_1 = "player1";
  private static final String PLAYER_NAME_2 = "player2";
  private static final String MAP_NAME = "SCMP_001";
  private static final int FAF_MOD_ID = 1;
  private static final int NEXT_GAME_ID = 1;
  private static final String QAI = "QAI";
  private static final String AIX = "AIX";
  private static final String OPTION_FACTION = "Faction";
  private static final String OPTION_SLOT = "Slot";
  private static final String OPTION_ARMY = "Army";

  private GameService instance;

  @Mock
  private GameRepository gameRepository;
  @Mock
  private ClientService clientService;
  @Mock
  private MapService mapService;
  @Mock
  private ModService modService;
  @Mock
  private ArmyStatisticsService armyStatisticsService;
  @Mock
  private RatingService ratingService;
  @Mock
  private PlayerService playerService;

  private Player player1;
  private Player player2;
  private Game game;

  @Before
  public void setUp() throws Exception {
    MapVersion map = new MapVersion();
    map.setRanked(true);

    game = new Game();
    game.setId(1);
    game.setMap(map);
    game.setFeaturedMod(new FeaturedMod());
    game.setVictoryCondition(VictoryCondition.DEMORALIZATION);
    game.setStartTime(Timestamp.from(Instant.now().plusSeconds(999)));
    game.getOptions().put(GameService.OPTION_FOG_OF_WAR, "explored");
    game.getOptions().put(GameService.OPTION_CHEATS_ENABLED, "false");
    game.getOptions().put(GameService.OPTION_PREBUILT_UNITS, "Off");
    game.getOptions().put(GameService.OPTION_NO_RUSH, "Off");
    game.getOptions().put(GameService.OPTION_RESTRICTED_CATEGORIES, 0);

    player1 = new Player();
    player1.setId(1);
    player1.setLogin(PLAYER_NAME_1);
    game.setHost(player1);
    addPlayer(game, player1, 2);

    player2 = new Player();
    player2.setId(2);
    player2.setLogin(PLAYER_NAME_2);

    ServerProperties serverProperties = new ServerProperties();

    FeaturedMod fafFeaturedMod = new FeaturedMod();
    fafFeaturedMod.setId(FAF_MOD_ID);

    when(gameRepository.findMaxId()).thenReturn(Optional.of(NEXT_GAME_ID));
    when(mapService.findMap(anyString())).thenReturn(Optional.empty());
    when(modService.getFeaturedMod(FAF_MOD_ID)).thenReturn(Optional.of(fafFeaturedMod));
    when(playerService.getPlayer(anyInt())).thenReturn(Optional.empty());

    instance = new GameService(gameRepository, clientService, mapService, modService, playerService, ratingService, serverProperties, armyStatisticsService);
    instance.postConstruct();
  }

  @Test
  public void createGame() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Game title", FAF_MOD_ID, MAP_NAME, "secret", GameVisibility.PUBLIC, player1);

    Optional<Game> optional = instance.getGame(NEXT_GAME_ID);
    assertThat(optional.isPresent(), is(true));
    Game game = optional.get();

    verify(clientService).startGameProcess(game, player1);
    assertThat(game.getTitle(), is("Game title"));
    assertThat(game.getHost(), is(player1));
    assertThat(game.getFeaturedMod().getId(), is(FAF_MOD_ID));
    assertThat(game.getMap(), is(nullValue()));
    assertThat(game.getMapName(), is(MAP_NAME));
    assertThat(game.getPassword(), is("secret"));
    assertThat(game.getState(), is(GameState.INITIALIZING));
    assertThat(game.getGameVisibility(), is(GameVisibility.PUBLIC));
    assertThat(player1.getCurrentGame(), is(game));
  }

  @Test
  public void joinGame() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Test game", FAF_MOD_ID, MAP_NAME, null, GameVisibility.PUBLIC, player1);
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player1);
    instance.joinGame(NEXT_GAME_ID, player2);

    Game game = instance.getGame(NEXT_GAME_ID).orElseThrow(() -> new IllegalStateException("Game not found"));

    verify(clientService).startGameProcess(game, player1);
    assertThat(player1.getCurrentGame(), is(game));
  }

  @Test
  public void updateGameStateIdle() throws Exception {
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player1);

    assertThat(game.getState(), is(GameState.OPEN));
  }

  @Test
  public void updateGameOption() throws Exception {
    assertThat(game.getOptions().containsKey("GameSpeed"), is(false));

    instance.updateGameOption(player1, "GameSpeed", "normal");

    assertThat(game.getOptions().get("GameSpeed"), is("normal"));
  }

  @Test
  public void updateGameOptionNotInGameIgnored() throws Exception {
    instance.updateGameOption(player2, "GameSpeed", "normal");
    verifyZeroInteractions(clientService);
  }

  @Test
  public void updatePlayerOption() throws Exception {
    assertThat(game.getPlayerOptions().containsKey(player1.getId()), is(false));

    instance.updatePlayerOption(player1, player1.getId(), OPTION_FACTION, 1);

    assertThat(game.getPlayerOptions().get(player1.getId()).get(OPTION_FACTION), is(1));
  }

  @Test
  public void updateAiOption() throws Exception {
    assertThat(game.getAiOptions().containsKey(QAI), is(false));

    instance.updateAiOption(player1, QAI, OPTION_FACTION, 2);

    assertThat(game.getAiOptions().get(QAI).get(OPTION_FACTION), is(2));
  }

  @Test
  public void clearSlot() throws Exception {
    instance.updateAiOption(player1, QAI, OPTION_FACTION, 1);
    instance.updateAiOption(player1, QAI, OPTION_SLOT, 1);
    instance.updateAiOption(player1, AIX, OPTION_FACTION, 2);
    instance.updateAiOption(player1, AIX, OPTION_SLOT, 2);

    instance.updatePlayerOption(player1, 1, OPTION_SLOT, 3);
    instance.updatePlayerOption(player1, 1, OPTION_FACTION, 3);
    instance.updatePlayerOption(player1, 2, OPTION_SLOT, 4);
    instance.updatePlayerOption(player1, 2, OPTION_FACTION, 4);

    instance.clearSlot(game, 1);
    assertThat(game.getPlayerOptions().containsKey(1), is(true));
    assertThat(game.getPlayerOptions().containsKey(2), is(true));
    assertThat(game.getAiOptions().containsKey(QAI), is(false));
    assertThat(game.getAiOptions().containsKey(AIX), is(true));

    instance.clearSlot(game, 4);
    assertThat(game.getPlayerOptions().containsKey(1), is(true));
    assertThat(game.getPlayerOptions().containsKey(2), is(false));
    assertThat(game.getAiOptions().containsKey(QAI), is(false));
    assertThat(game.getAiOptions().containsKey(AIX), is(true));

    instance.clearSlot(game, 3);
    assertThat(game.getPlayerOptions().containsKey(1), is(false));
    assertThat(game.getPlayerOptions().containsKey(2), is(false));
    assertThat(game.getAiOptions().containsKey(QAI), is(false));
    assertThat(game.getAiOptions().containsKey(AIX), is(true));

    instance.clearSlot(game, 2);
    assertThat(game.getPlayerOptions().containsKey(1), is(false));
    assertThat(game.getPlayerOptions().containsKey(2), is(false));
    assertThat(game.getAiOptions().containsKey(QAI), is(false));
    assertThat(game.getAiOptions().containsKey(AIX), is(false));
  }

  @Test
  public void reportDesync() throws Exception {
    assertThat(game.getDesyncCounter().intValue(), is(0));
    instance.reportDesync(player1);
    instance.reportDesync(player1);
    instance.reportDesync(player1);
    assertThat(game.getDesyncCounter().intValue(), is(3));
  }

  @Test
  public void updateGameMods() throws Exception {
    instance.updateGameMods(game, Arrays.asList("1-2-3-4", "5-6-7-8"));

    List<String> simMods = game.getSimMods();
    assertThat(simMods, hasItem("1-2-3-4"));
    assertThat(simMods, hasItem("5-6-7-8"));
  }

  @Test
  public void updateGameModsCountClearsIfZero() throws Exception {
    instance.updateGameMods(game, Arrays.asList("1-2-3-4", "5-6-7-8"));
    instance.updateGameModsCount(game, 0);

    assertThat(game.getSimMods(), is(empty()));
  }

  @Test
  public void updateGameModsCountDoesntClearIfNonZero() throws Exception {
    instance.updateGameMods(game, Arrays.asList("1-2-3-4", "5-6-7-8"));
    instance.updateGameModsCount(game, 1);

    assertThat(game.getSimMods(), hasItems("1-2-3-4", "5-6-7-8"));
  }

  @Test
  public void reportArmyScore() throws Exception {
    player2.setCurrentGame(game);
    instance.updatePlayerOption(player1, player1.getId(), OPTION_ARMY, 1);
    instance.updatePlayerOption(player1, player2.getId(), OPTION_ARMY, 2);

    instance.reportArmyScore(player1, 1, 10);
    instance.reportArmyScore(player1, 2, -1);
    instance.reportArmyScore(player2, 1, 10);
    instance.reportArmyScore(player2, 2, -1);

    assertThat(game.getReportedArmyScores().values(), hasSize(2));
    assertThat(game.getReportedArmyScores().get(player1.getId()), hasSize(2));
    assertThat(game.getReportedArmyScores().get(player2.getId()), hasSize(2));
  }

  @Test
  public void reportArmyOutcome() throws Exception {
    player2.setCurrentGame(game);
    instance.updatePlayerOption(player1, player1.getId(), OPTION_ARMY, 1);
    instance.updatePlayerOption(player1, player2.getId(), OPTION_ARMY, 2);

    instance.reportArmyOutcome(player1, 1, Outcome.VICTORY);
    instance.reportArmyOutcome(player1, 2, Outcome.DEFEAT);
    instance.reportArmyOutcome(player2, 1, Outcome.VICTORY);
    instance.reportArmyOutcome(player2, 2, Outcome.DEFEAT);

    assertThat(game.getReportedArmyOutcomes().values(), hasSize(2));
    assertThat(game.getReportedArmyOutcomes().get(player1.getId()), hasSize(2));
    assertThat(game.getReportedArmyOutcomes().get(player2.getId()), hasSize(2));
  }

  @Test
  public void reportArmyStatistics() throws Exception {
    assertThat(game.getArmyStatistics(), is(empty()));
    instance.reportArmyStatistics(player1, Arrays.asList(new ArmyStatistics(), new ArmyStatistics()));
    assertThat(game.getArmyStatistics(), is(notNullValue()));
    assertThat(game.getArmyStatistics(), hasSize(2));
  }

  @Test
  public void enforceRating() throws Exception {
    assertThat(game.isRatingEnforced(), is(false));
    instance.enforceRating(player1);
    assertThat(game.isRatingEnforced(), is(true));
  }

  @Test
  public void endGameIfNoPlayerConnected() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Game title", FAF_MOD_ID, MAP_NAME, "secret", GameVisibility.PUBLIC, player1);
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player1);

    Game game = instance.getGame(NEXT_GAME_ID).orElseThrow(() -> new IllegalStateException("No game found"));
    assertThat(game.getState(), is(GameState.OPEN));

    instance.joinGame(NEXT_GAME_ID, player2);
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player2);
    assertThat(game.getState(), is(GameState.OPEN));

    instance.updatePlayerGameState(PlayerGameState.ENDED, player1);
    assertThat(game.getState(), is(GameState.OPEN));

    instance.updatePlayerGameState(PlayerGameState.ENDED, player2);
    assertThat(game.getState(), is(GameState.CLOSED));

    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
    assertThat(player2.getCurrentGame(), is(nullValue()));
    assertThat(player2.getGameState(), is(PlayerGameState.NONE));
  }

  @Test
  public void onGameEndedDoesntSaveGameIfGameDidntStart() throws Exception {
    instance.updatePlayerGameState(PlayerGameState.ENDED, player1);
    assertThat(game.getState(), is(GameState.CLOSED));

    verify(gameRepository, never()).save(any(Game.class));
    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
  }

  @Test
  public void onGameEndedDoesntProcessArmyStatsIfGameDidntStart() throws Exception {
    instance.updatePlayerGameState(PlayerGameState.ENDED, player1);
    assertThat(game.getState(), is(GameState.CLOSED));

    verifyZeroInteractions(armyStatisticsService);
    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
  }

  @Test
  public void onGameEndedSavesGameIfGameStarted() throws Exception {
    game.setState(GameState.OPEN);
    game.setState(GameState.PLAYING);

    instance.updatePlayerGameState(PlayerGameState.ENDED, player1);
    assertThat(game.getState(), is(GameState.CLOSED));

    verify(gameRepository).save(game);
    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
  }

  @Test
  public void onGameEndedProcessesStatsIfGameStarted() throws Exception {
    game.setState(GameState.OPEN);
    game.setState(GameState.PLAYING);

    instance.updatePlayerGameState(PlayerGameState.ENDED, player1);
    assertThat(game.getState(), is(GameState.CLOSED));

    verify(armyStatisticsService).process(any(), eq(game), any());
    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
  }

  @Test
  public void onGameLaunching() throws Exception {
    game.setState(GameState.OPEN);
    game.setStartTime(null);
    player1.setGameState(PlayerGameState.LOBBY);

    instance.updatePlayerGameState(PlayerGameState.LAUNCHING, player1);

    assertThat(game.getState(), is(GameState.PLAYING));
    assertThat(game.getStartTime(), is(lessThan(Timestamp.from(Instant.now().plusSeconds(1)))));
    assertThat(game.getStartTime(), is(greaterThan(Timestamp.from(Instant.now().minusSeconds(10)))));

    verify(gameRepository).save(game);
    verify(clientService).sendDelayed(any(GameResponse.class), any(), any(), any());
  }

  @Test
  public void onGameLaunchingSentByNonHostIsIgnored() throws Exception {
    player2.setCurrentGame(game);
    player2.setGameState(PlayerGameState.LOBBY);
    game.setState(GameState.OPEN);

    instance.updatePlayerGameState(PlayerGameState.LAUNCHING, player2);

    assertThat(game.getState(), is(GameState.OPEN));
    verifyZeroInteractions(clientService);
    verify(gameRepository, never()).save(game);
  }

  @Test
  public void updateGameRankinessUnrankedMod() throws Exception {
    game.getSimMods().add("1-2-3-4");
    when(modService.isModRanked("1-2-3-4")).thenReturn(false);

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.BAD_MOD));
  }

  @Test
  public void updateGameRankinessUnrankedMap() throws Exception {
    game.getMap().setRanked(false);

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.BAD_MAP));
  }

  @Test
  public void updateGameRankinessWrongVictoryConditionDominationNotCoop() throws Exception {
    game.setVictoryCondition(VictoryCondition.DOMINATION);
    when(modService.isCoop(any())).thenReturn(false);

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.WRONG_VICTORY_CONDITION));
  }

  @Test
  public void updateGameRankinessWrongVictoryConditionEradicationNotCoop() throws Exception {
    game.setVictoryCondition(VictoryCondition.ERADICATION);
    when(modService.isCoop(any())).thenReturn(false);

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.WRONG_VICTORY_CONDITION));
  }

  @Test
  public void updateGameRankinessWrongVictoryConditionSandboxNotCoop() throws Exception {
    game.setVictoryCondition(VictoryCondition.SANDBOX);
    when(modService.isCoop(any())).thenReturn(false);

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.WRONG_VICTORY_CONDITION));
  }

  @Test
  public void updateGameRankinessUnitRestriction() throws Exception {
    game.getOptions().put(GameService.OPTION_RESTRICTED_CATEGORIES, 1);
    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.BAD_UNIT_RESTRICTIONS));
  }

  @Test
  public void updateGameRankinessFreeForAll() throws Exception {
    addPlayer(game, player2, 3);
    addPlayer(game, new Player(), 4);

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.FREE_FOR_ALL));
  }

  @Test
  public void updateGameRankinessUnevenTeams() throws Exception {
    addPlayer(game, player2, 2);
    addPlayer(game, new Player(), 3);

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.UNEVEN_TEAMS));
  }

  @Test
  public void updateGameRankinessRankedEvenTeams() throws Exception {
    addPlayer(game, player2, 2);
    addPlayer(game, new Player(), 3);
    addPlayer(game, new Player(), 3);

    game.getReportedArmyOutcomes().put(1, Collections.emptyList());
    game.getReportedArmyOutcomes().put(2, Collections.emptyList());
    game.getReportedArmyOutcomes().put(3, Collections.emptyList());
    game.getReportedArmyOutcomes().put(4, Collections.emptyList());

    game.getReportedArmyScores().put(1, Collections.emptyList());
    game.getReportedArmyScores().put(2, Collections.emptyList());
    game.getReportedArmyScores().put(3, Collections.emptyList());
    game.getReportedArmyScores().put(4, Collections.emptyList());

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.RANKED));
  }

  @Test
  public void updateGameRankinessNoFogOfWar() throws Exception {
    game.getOptions().put(GameService.OPTION_FOG_OF_WAR, "foo");

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.NO_FOG_OF_WAR));
  }

  @Test
  public void updateGameRankinessCheatsEnabled() throws Exception {
    game.getOptions().put(GameService.OPTION_CHEATS_ENABLED, "true");

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.CHEATS_ENABLED));
  }

  @Test
  public void updateGameRankinessPrebuiltEnabled() throws Exception {
    game.getOptions().put(GameService.OPTION_PREBUILT_UNITS, "On");

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.PREBUILT_ENABLED));
  }

  @Test
  public void updateGameRankinessNoRushEnabled() throws Exception {
    game.getOptions().put(GameService.OPTION_NO_RUSH, "On");

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.NO_RUSH_ENABLED));
  }

  @Test
  public void updateGameRankinessTooManyDesyncs() throws Exception {
    game.getDesyncCounter().set(5);

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.TOO_MANY_DESYNCS));
  }

  @Test
  public void updateGameRankinessMutualDraw() throws Exception {
    game.setMutuallyAgreedDraw(true);

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.MUTUAL_DRAW));
  }

  @Test
  public void updateGameRankinessSinglePlayer() throws Exception {
    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.SINGLE_PLAYER));
  }

  @Test
  public void updateGameRankinessUnknownResult() throws Exception {
    addPlayer(game, player2, 3);
    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.UNKNOWN_RESULT));
  }

  @Test
  public void updateGameRankinessTooShort() throws Exception {
    addPlayer(game, player1, 1);
    game.getReportedArmyScores().put(1, Collections.singletonList(new ArmyScore(1, 10)));
    game.getReportedArmyOutcomes().put(1, Collections.singletonList(new ArmyOutcome(1, Outcome.VICTORY)));
    game.setStartTime(Timestamp.from(Instant.now()));

    instance.updateGameRankiness(game);

    assertThat(game.getRankiness(), is(Rankiness.TOO_SHORT));
  }

  @Test(expected = IllegalStateException.class)
  public void updateGameRankinessAlreadySetThrowsException() throws Exception {
    game.setRankiness(Rankiness.UNKNOWN_RESULT);
    instance.updateGameRankiness(game);
  }

  @Test
  public void onClientDisconnectRemovesPlayerAndUnsetsGameAndRemovesGameIfLastPlayer() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Test game", FAF_MOD_ID, MAP_NAME, null, GameVisibility.PUBLIC, player1);
    assertThat(player1.getCurrentGame(), is(notNullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
    assertThat(instance.getGame(NEXT_GAME_ID).isPresent(), is(true));

    User user = new User();
    user.setPassword("pw");
    user.setLogin("JUnit");
    user.setPlayer(player1);

    ClientConnection clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class))
      .setUserDetails(new FafUserDetails(user));
    instance.onClientDisconnect(new ClientDisconnectedEvent(this, clientConnection));

    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
    assertThat(instance.getGame(NEXT_GAME_ID).isPresent(), is(false));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void onAuthenticationSuccess() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Test game", FAF_MOD_ID, MAP_NAME, null, GameVisibility.PUBLIC, player1);

    ClientConnection clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class));
    TestingAuthenticationToken authentication = new TestingAuthenticationToken("JUnit", "foo");
    authentication.setDetails(player2.setClientConnection(clientConnection));

    instance.onAuthenticationSuccess(new AuthenticationSuccessEvent(authentication));

    ArgumentCaptor<Collection<GameResponse>> captor = ArgumentCaptor.forClass((Class) Collection.class);
    verify(clientService).sendGameList(captor.capture(), eq(player2));
    Collection<GameResponse> games = captor.getValue();

    assertThat(games, hasSize(1));
    assertThat(games.iterator().next().getTitle(), is("Test game"));
  }

  /**
   * Tests whether all but the affected player are informed to drop someone.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void disconnectFromGame() throws Exception {
    Player player4 = new Player();
    Player player3 = (Player) new Player()
      .setCurrentGame(game)
      .setId(3);

    Map<Integer, Player> activePlayers = game.getActivePlayers();
    activePlayers.put(1, player1);
    activePlayers.put(2, player2);
    activePlayers.put(3, player3);
    activePlayers.put(4, player4);

    when(playerService.getPlayer(3)).thenReturn(Optional.of(player3));

    instance.disconnectFromGame(new User(), 3);

    ArgumentCaptor<List<ConnectionAware>> captor = ArgumentCaptor.forClass((Class) List.class);
    verify(clientService).disconnectPlayer(eq(3), captor.capture());
    List<ConnectionAware> recipients = captor.getValue();

    assertThat(recipients, hasSize(3));
    assertThat(recipients, hasItems(
      player1, player2, player4
    ));
  }

  @Test
  public void disconnectFromGameIgnoredWhenPlayerUnknown() throws Exception {
    instance.disconnectFromGame(new User(), 412312);
    verifyZeroInteractions(clientService);
  }

  @Test
  public void disconnectFromGameIgnoredWhenPlayerNotInGame() throws Exception {
    when(playerService.getPlayer(3)).thenReturn(Optional.of(new Player()));
    instance.disconnectFromGame(new User(), 3);
    verifyZeroInteractions(clientService);
  }

  private void addPlayer(Game game, Player player, int team) {
    player.setCurrentGame(game);

    GamePlayerStats gamePlayerStats = new GamePlayerStats();
    gamePlayerStats.setTeam(team);
    gamePlayerStats.setPlayer(player);
    game.getPlayerStats().add(gamePlayerStats);
  }
}
