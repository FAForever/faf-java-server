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
import com.faforever.server.entity.GameState;
import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.MapVersion;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.entity.Validity;
import com.faforever.server.entity.VictoryCondition;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.integration.Protocol;
import com.faforever.server.map.MapService;
import com.faforever.server.mod.ModService;
import com.faforever.server.player.PlayerService;
import com.faforever.server.rating.RatingService;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.stats.ArmyStatistics;
import com.faforever.server.stats.ArmyStatisticsService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import javax.persistence.EntityManager;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.faforever.server.error.RequestExceptionWithCode.requestExceptionWithCode;
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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
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
  private static final String FAF_TECHNICAL_NAME = "faf";
  private static final int NEXT_GAME_ID = 1;
  private static final String QAI = "QAI";
  private static final String AIX = "AIX";
  private static final String OPTION_FACTION = "Faction";
  private static final String OPTION_SLOT = "Slot";
  private static final String OPTION_ARMY = "Army";
  private static final int GAME_MIN_RATING = 1000;
  private static final int GAME_MAX_RATING = 1500;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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
  @Mock
  private EntityManager entityManager;

  private Player player1;
  private Player player2;
  private Game game;
  private ServerProperties serverProperties;

  @Before
  public void setUp() throws Exception {
    MapVersion map = new MapVersion();
    map.setRanked(true);

    game = new Game()
      .setId(1)
      .setMap(map)
      .setFeaturedMod(new FeaturedMod())
      .setVictoryCondition(VictoryCondition.DEMORALIZATION)
      .setStartTime(Timestamp.from(Instant.now().plusSeconds(999)));
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

    FeaturedMod fafFeaturedMod = new FeaturedMod();
    fafFeaturedMod.setTechnicalName(FAF_TECHNICAL_NAME);

    when(gameRepository.findMaxId()).thenReturn(Optional.of(NEXT_GAME_ID - 1));
    when(mapService.findMap(anyString())).thenReturn(Optional.empty());
    when(modService.getFeaturedMod(FAF_TECHNICAL_NAME)).thenReturn(Optional.of(fafFeaturedMod));
    when(playerService.getOnlinePlayer(anyInt())).thenReturn(Optional.empty());
    doAnswer(invocation -> invocation.getArgumentAt(0, Player.class).setGlobalRating(new GlobalRating()))
      .when(ratingService).initGlobalRating(any());

    serverProperties = new ServerProperties();
    serverProperties.getGame().setRankedMinTimeMultiplicator(-1);

    instance = new GameService(gameRepository, clientService, mapService, modService, playerService, ratingService, serverProperties, entityManager, armyStatisticsService);
    instance.onApplicationEvent(null);
  }

  @Test
  public void createGame() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Game title", FAF_TECHNICAL_NAME, MAP_NAME, "secret", GameVisibility.PUBLIC, GAME_MIN_RATING, GAME_MAX_RATING, player1);

    Optional<Game> optional = instance.getActiveGame(NEXT_GAME_ID);
    assertThat(optional.isPresent(), is(true));
    Game game = optional.get();

    verify(clientService).startGameProcess(game, player1);
    assertThat(game.getTitle(), is("Game title"));
    assertThat(game.getHost(), is(player1));
    assertThat(game.getFeaturedMod().getTechnicalName(), is(FAF_TECHNICAL_NAME));
    assertThat(game.getMap(), is(nullValue()));
    assertThat(game.getMapName(), is(MAP_NAME));
    assertThat(game.getPassword(), is("secret"));
    assertThat(game.getState(), is(GameState.INITIALIZING));
    assertThat(game.getGameVisibility(), is(GameVisibility.PUBLIC));
    assertThat(game.getMinRating(), is(GAME_MIN_RATING));
    assertThat(game.getMaxRating(), is(GAME_MAX_RATING));
    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameBeingJoined(), is(game));
  }

  @Test
  public void joinGame() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Test game", FAF_TECHNICAL_NAME, MAP_NAME, null, GameVisibility.PUBLIC, GAME_MIN_RATING, GAME_MAX_RATING, player1);
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player1);
    instance.joinGame(NEXT_GAME_ID, player2);

    Game game = instance.getActiveGame(NEXT_GAME_ID).orElseThrow(() -> new IllegalStateException("Game not found"));

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
    addPlayer(game, player1, 2);

    instance.updatePlayerOption(player1, player1.getId(), OPTION_FACTION, 1);
    assertThat(game.getPlayerOptions().get(player1.getId()).get(OPTION_FACTION), is(1));

    instance.updatePlayerOption(player1, player1.getId(), OPTION_FACTION, 2);
    assertThat(game.getPlayerOptions().get(player1.getId()).get(OPTION_FACTION), is(2));
  }

  @Test
  public void updateAiOption() throws Exception {
    assertThat(game.getAiOptions().containsKey(QAI), is(false));

    instance.updateAiOption(player1, QAI, OPTION_FACTION, 2);

    assertThat(game.getAiOptions().get(QAI).get(OPTION_FACTION), is(2));
  }

  @Test
  public void clearSlot() throws Exception {
    addPlayer(game, player1, 2);
    addPlayer(game, player2, 3);

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
    addPlayer(game, player1, 2);
    addPlayer(game, player2, 3);

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
  public void reportArmyScoreAiScore() throws Exception {
    addPlayer(game, player1, 2);

    instance.updatePlayerOption(player1, player1.getId(), OPTION_ARMY, 1);
    instance.updateAiOption(player1, "QAI", OPTION_ARMY, 2);

    instance.reportArmyScore(player1, 1, 10);
    instance.reportArmyScore(player1, 2, -1);

    assertThat(game.getReportedArmyScores().values(), hasSize(1));
    assertThat(game.getReportedArmyScores().get(player1.getId()), hasSize(2));
  }

  @Test
  public void reportArmyScoreNotInGame() throws Exception {
    assertThat(player2.getCurrentGame(), is(nullValue()));

    instance.reportArmyScore(player2, 1, 10);

    assertThat(game.getReportedArmyScores().values(), is(empty()));
  }

  @Test
  public void reportArmyOutcome() throws Exception {
    addPlayer(game, player1, 2);
    addPlayer(game, player2, 3);

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
    instance.createGame("Game title", FAF_TECHNICAL_NAME, MAP_NAME, "secret", GameVisibility.PUBLIC, GAME_MIN_RATING, GAME_MAX_RATING, player1);
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player1);

    Game game = instance.getActiveGame(NEXT_GAME_ID).orElseThrow(() -> new IllegalStateException("No game found"));
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
    launchGame();

    instance.updatePlayerGameState(PlayerGameState.ENDED, player1);
    assertThat(game.getState(), is(GameState.CLOSED));

    verify(armyStatisticsService).process(any(), eq(game), any());
    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
  }

  @Test
  public void onGameLaunching() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Test game", FAF_TECHNICAL_NAME, MAP_NAME, null, GameVisibility.PUBLIC, GAME_MIN_RATING, GAME_MAX_RATING, player1);
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player1);

    Game game = instance.getActiveGame(NEXT_GAME_ID).get();

    instance.updatePlayerOption(player1, player1.getId(), OPTION_FACTION, 1);
    instance.updatePlayerGameState(PlayerGameState.LAUNCHING, player1);

    assertThat(game.getState(), is(GameState.PLAYING));
    assertThat(game.getStartTime(), is(lessThan(Timestamp.from(Instant.now().plusSeconds(1)))));
    assertThat(game.getStartTime(), is(greaterThan(Timestamp.from(Instant.now().minusSeconds(10)))));

    verify(entityManager).persist(game);
    verify(clientService, atLeastOnce()).sendDelayed(any(GameResponse.class), any(), any(), any());
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
  public void updateGameValidityUnrankedMod() throws Exception {
    game.getSimMods().add("1-2-3-4");
    when(modService.isModRanked("1-2-3-4")).thenReturn(false);

    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.BAD_MOD));
  }

  @Test
  public void updateGameValidityUnrankedMap() throws Exception {
    game.getMap().setRanked(false);

    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.BAD_MAP));
  }

  @Test
  public void updateGameValidityWrongVictoryConditionDominationNotCoop() throws Exception {
    game.setVictoryCondition(VictoryCondition.DOMINATION);
    when(modService.isCoop(any())).thenReturn(false);

    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.WRONG_VICTORY_CONDITION));
  }

  @Test
  public void updateGameValidityWrongVictoryConditionEradicationNotCoop() throws Exception {
    game.setVictoryCondition(VictoryCondition.ERADICATION);
    when(modService.isCoop(any())).thenReturn(false);

    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.WRONG_VICTORY_CONDITION));
  }

  @Test
  public void updateGameValidityWrongVictoryConditionSandboxNotCoop() throws Exception {
    game.setVictoryCondition(VictoryCondition.SANDBOX);
    when(modService.isCoop(any())).thenReturn(false);

    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.WRONG_VICTORY_CONDITION));
  }

  @Test
  public void updateGameValidityUnitRestriction() throws Exception {
    addPlayer(game, player1, 2);
    addPlayer(game, player2, 3);
    game.getOptions().put(GameService.OPTION_RESTRICTED_CATEGORIES, 1);

    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.BAD_UNIT_RESTRICTIONS));
  }

  @Test
  public void updateGameValidityFreeForAll() throws Exception {
    // Every player is in a different team.
    addPlayer(game, player1, 2);
    addPlayer(game, player2, 3);
    addPlayer(game, new Player(), 4);

    launchGame();

    game.getReportedArmyScores().put(1, Collections.emptyList());
    game.getReportedArmyScores().put(2, Collections.emptyList());
    game.getReportedArmyOutcomes().put(1, Collections.emptyList());
    game.getReportedArmyOutcomes().put(2, Collections.emptyList());

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.FREE_FOR_ALL));
  }

  @Test
  public void updateGameValidityUnevenTeams() throws Exception {
    // player1 is already in team 2
    addPlayer(game, player2, 2);
    addPlayer(game, new Player(), 3);

    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.UNEVEN_TEAMS));
  }

  @Test
  public void updateGameValidityRankedEvenTeams() throws Exception {
    addPlayer(game, player1, 2);
    addPlayer(game, player2, 2);
    addPlayer(game, (Player) new Player().setId(3), 3);
    addPlayer(game, (Player) new Player().setId(4), 3);

    launchGame();

    game.getReportedArmyOutcomes().put(1, Collections.emptyList());
    game.getReportedArmyOutcomes().put(2, Collections.emptyList());
    game.getReportedArmyOutcomes().put(3, Collections.emptyList());
    game.getReportedArmyOutcomes().put(4, Collections.emptyList());

    game.getReportedArmyScores().put(1, Collections.emptyList());
    game.getReportedArmyScores().put(2, Collections.emptyList());
    game.getReportedArmyScores().put(3, Collections.emptyList());
    game.getReportedArmyScores().put(4, Collections.emptyList());

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.RANKED));
  }

  @Test
  public void updateGameValidityNoFogOfWar() throws Exception {
    game.getOptions().put(GameService.OPTION_FOG_OF_WAR, "foo");
    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.NO_FOG_OF_WAR));
  }

  @Test
  public void updateGameValidityCheatsEnabled() throws Exception {
    game.getOptions().put(GameService.OPTION_CHEATS_ENABLED, "true");

    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.CHEATS_ENABLED));
  }

  @Test
  public void updateGameValidityPrebuiltEnabled() throws Exception {
    game.getOptions().put(GameService.OPTION_PREBUILT_UNITS, "On");

    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.PREBUILT_ENABLED));
  }

  @Test
  public void updateGameValidityNoRushEnabled() throws Exception {
    game.getOptions().put(GameService.OPTION_NO_RUSH, "On");

    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.NO_RUSH_ENABLED));
  }

  @Test
  public void updateGameValidityTooManyDesyncs() throws Exception {
    game.getDesyncCounter().set(5);
    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.TOO_MANY_DESYNCS));
  }

  @Test
  public void updateGameValidityMutualDraw() throws Exception {
    game.setMutuallyAgreedDraw(true);

    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.MUTUAL_DRAW));
  }

  @Test
  public void updateGameValiditySinglePlayer() throws Exception {
    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.SINGLE_PLAYER));
  }

  @Test
  public void updateGameValidityUnknownResult() throws Exception {
    addPlayer(game, player1, 2);
    addPlayer(game, player2, 3);
    launchGame();

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.UNKNOWN_RESULT));
  }

  @Test
  public void updateGameValidityTooShort() throws Exception {
    addPlayer(game, player1, 1);
    addPlayer(game, player2, 2);

    launchGame();

    game.getReportedArmyScores().put(1, Collections.singletonList(new ArmyScore(1, 10)));
    game.getReportedArmyOutcomes().put(1, Collections.singletonList(new ArmyOutcome(1, Outcome.VICTORY)));

    serverProperties.getGame().setRankedMinTimeMultiplicator(10_000);
    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.TOO_SHORT));
  }

  @Test(expected = IllegalStateException.class)
  public void updateGameValidityAlreadySetThrowsException() throws Exception {
    game.setValidity(Validity.UNKNOWN_RESULT);
    instance.updateGameValidity(game);
  }

  @Test
  public void onClientDisconnectRemovesPlayerAndUnsetsGameAndRemovesGameIfLastPlayer() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Test game", FAF_TECHNICAL_NAME, MAP_NAME, null, GameVisibility.PUBLIC, GAME_MIN_RATING, GAME_MAX_RATING, player1);
    assertThat(player1.getGameBeingJoined(), is(notNullValue()));
    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
    assertThat(instance.getActiveGame(NEXT_GAME_ID).isPresent(), is(true));

    User user = new User();
    user.setPassword("pw");
    user.setLogin("JUnit");
    user.setPlayer(player1);

    ClientConnection clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class))
      .setUserDetails(new FafUserDetails(user));
    instance.onClientDisconnect(new ClientDisconnectedEvent(this, clientConnection));

    assertThat(player1.getGameBeingJoined(), is(nullValue()));
    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
    assertThat(instance.getActiveGame(NEXT_GAME_ID).isPresent(), is(false));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void onAuthenticationSuccess() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Test game", FAF_TECHNICAL_NAME, MAP_NAME, null, GameVisibility.PUBLIC, GAME_MIN_RATING, GAME_MAX_RATING, player1);

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

    addPlayer(game, player1, 2);
    addPlayer(game, player2, 3);
    addPlayer(game, player3, 2);
    addPlayer(game, player4, 3);

    when(playerService.getOnlinePlayer(3)).thenReturn(Optional.of(player3));

    instance.disconnectPlayerFromGame(new User(), 3);

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
    instance.disconnectPlayerFromGame(new User(), 412312);
    verifyZeroInteractions(clientService);
  }

  @Test
  public void disconnectFromGameIgnoredWhenPlayerNotInGame() throws Exception {
    when(playerService.getOnlinePlayer(3)).thenReturn(Optional.of(new Player()));
    instance.disconnectPlayerFromGame(new User(), 3);
    verifyZeroInteractions(clientService);
  }

  @Test
  public void restoreGameSessionWasNeverInGame() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Game title", FAF_TECHNICAL_NAME, MAP_NAME, "secret", GameVisibility.PUBLIC, GAME_MIN_RATING, GAME_MAX_RATING, player1);
    launchGame();

    Game game = instance.getActiveGame(NEXT_GAME_ID).get();

    expectedException.expect(requestExceptionWithCode(ErrorCode.CANT_RESTORE_GAME_NOT_PARTICIPANT));

    instance.restoreGameSession(player2, game.getId());
  }

  @Test
  public void restoreGameSessionOpenGame() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Game title", FAF_TECHNICAL_NAME, MAP_NAME, "secret", GameVisibility.PUBLIC, GAME_MIN_RATING, GAME_MAX_RATING, player1);
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player1);

    Game game = instance.getActiveGame(NEXT_GAME_ID).get();
    instance.joinGame(game.getId(), player2);
    assertThat(player2.getGameBeingJoined(), is(game));
    assertThat(player2.getCurrentGame(), is(nullValue()));

    instance.updatePlayerGameState(PlayerGameState.LOBBY, player2);
    instance.updatePlayerGameState(PlayerGameState.LAUNCHING, player1);
    assertThat(player2.getCurrentGame(), is(game));

    ClientConnection clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class));
    clientConnection.setUserDetails(new FafUserDetails((User) new User().setPlayer(player2).setPassword("pw").setLogin("JUnit")));
    player2.setClientConnection(clientConnection);
    instance.onClientDisconnect(new ClientDisconnectedEvent(this, player2.getClientConnection()));
    assertThat(player2.getGameBeingJoined(), is(nullValue()));
    assertThat(player2.getCurrentGame(), is(nullValue()));

    instance.restoreGameSession(player2, game.getId());
    assertThat(player2.getCurrentGame(), is(game));
  }

  @Test
  public void restoreGameSessionPlayingGame() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Game title", FAF_TECHNICAL_NAME, MAP_NAME, "secret", GameVisibility.PUBLIC, GAME_MIN_RATING, GAME_MAX_RATING, player1);
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player1);

    Game game = instance.getActiveGame(NEXT_GAME_ID).get();
    instance.joinGame(game.getId(), player2);
    assertThat(player2.getGameBeingJoined(), is(game));

    instance.updatePlayerGameState(PlayerGameState.LOBBY, player2);
    instance.updatePlayerGameState(PlayerGameState.LAUNCHING, player1);

    ClientConnection clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16, mock(InetAddress.class));
    clientConnection.setUserDetails(new FafUserDetails((User) new User().setPlayer(player2).setPassword("pw").setLogin("JUnit")));
    player2.setClientConnection(clientConnection);
    instance.onClientDisconnect(new ClientDisconnectedEvent(this, player2.getClientConnection()));
    assertThat(player2.getCurrentGame(), is(nullValue()));

    instance.restoreGameSession(player2, game.getId());
    assertThat(player2.getCurrentGame(), is(game));
  }

  private void launchGame() {
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player1);
    instance.updatePlayerGameState(PlayerGameState.LAUNCHING, player1);
  }

  private void addPlayer(Game game, Player player, int team) {
    player.setCurrentGame(game);
    game.getConnectedPlayers().put(player.getId(), player);
    game.getPlayerOptions().computeIfAbsent(player.getId(), integer -> new HashMap<>()).put(GameService.OPTION_TEAM, team);
  }
}
