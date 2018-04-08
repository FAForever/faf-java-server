package com.faforever.server.game;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientService;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.client.GameResponses;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.ArmyResult;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.GameState;
import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.Ladder1v1Rating;
import com.faforever.server.entity.MapVersion;
import com.faforever.server.entity.Mod;
import com.faforever.server.entity.ModVersion;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.entity.Validity;
import com.faforever.server.entity.VictoryCondition;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.integration.Protocol;
import com.faforever.server.ladder1v1.DivisionService;
import com.faforever.server.map.MapService;
import com.faforever.server.mod.ModService;
import com.faforever.server.player.PlayerOnlineEvent;
import com.faforever.server.player.PlayerService;
import com.faforever.server.rating.RatingService;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.stats.ArmyStatistics;
import com.faforever.server.stats.ArmyStatisticsService;
import com.faforever.server.stats.Metrics;
import com.google.common.collect.ImmutableMap;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;

import javax.persistence.EntityManager;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.faforever.server.error.RequestExceptionWithCode.requestExceptionWithCode;
import static com.faforever.server.game.GameService.OPTION_ARMY;
import static com.faforever.server.game.GameService.OPTION_CHEATS_ENABLED;
import static com.faforever.server.game.GameService.OPTION_COLOR;
import static com.faforever.server.game.GameService.OPTION_FACTION;
import static com.faforever.server.game.GameService.OPTION_FOG_OF_WAR;
import static com.faforever.server.game.GameService.OPTION_NO_RUSH;
import static com.faforever.server.game.GameService.OPTION_PREBUILT_UNITS;
import static com.faforever.server.game.GameService.OPTION_RESTRICTED_CATEGORIES;
import static com.faforever.server.game.GameService.OPTION_SLOT;
import static com.faforever.server.game.GameService.OPTION_START_SPOT;
import static com.faforever.server.game.GameService.OPTION_TEAM;
import static com.faforever.server.game.GameService.OPTION_VICTORY_CONDITION;
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
  private static final String QAI = "QAI";
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
  private DivisionService divisionService;
  @Mock
  private EntityManager entityManager;

  private MeterRegistry meterRegistry;
  private Player player1;
  private Player player2;
  private ServerProperties serverProperties;

  @Before
  public void setUp() throws Exception {
    meterRegistry = new SimpleMeterRegistry();

    MapVersion map = new MapVersion();
    map.setRanked(true);

    player1 = new Player();
    player1.setId(1);
    player1.setLogin(PLAYER_NAME_1);

    player2 = new Player();
    player2.setId(2);
    player2.setLogin(PLAYER_NAME_2);

    FeaturedMod fafFeaturedMod = new FeaturedMod();
    fafFeaturedMod.setTechnicalName(FAF_TECHNICAL_NAME);

    when(gameRepository.findMaxId()).thenReturn(Optional.empty());
    when(mapService.findMap(anyString())).thenReturn(Optional.empty());
    when(mapService.findMap(MAP_NAME)).thenReturn(Optional.of(map));
    when(modService.getFeaturedMod(FAF_TECHNICAL_NAME)).thenReturn(Optional.of(fafFeaturedMod));
    when(playerService.getOnlinePlayer(anyInt())).thenReturn(Optional.empty());
    doAnswer(invocation -> ((Player) invocation.getArgument(0)).setGlobalRating(new GlobalRating()))
      .when(ratingService).initGlobalRating(any());

    serverProperties = new ServerProperties();
    serverProperties.getGame().setRankedMinTimeMultiplicator(-10000);

    instance = new GameService(gameRepository, meterRegistry, clientService, mapService, modService, playerService, ratingService,
      serverProperties, divisionService, entityManager, armyStatisticsService);
    instance.onApplicationEvent(null);
  }

  @Test
  public void joinGame() throws Exception {
    Game game = hostGame(player1);

    instance.joinGame(game.getId(), game.getPassword(), player2);
    verify(clientService).startGameProcess(game, player2);
    assertThat(player2.getCurrentGame(), is(game));
    assertThat(player2.getGameState(), is(PlayerGameState.INITIALIZING));

    instance.updatePlayerGameState(PlayerGameState.LOBBY, player2);
    assertThat(player2.getCurrentGame(), is(game));
  }

  @Test
  public void joinGameWithPassword() throws Exception {
    Game game = hostGame(player1);
    game.setPassword("pw");

    instance.joinGame(game.getId(), "pw", player2);
    verify(clientService).startGameProcess(game, player2);
    assertThat(player2.getCurrentGame(), is(game));
    assertThat(player2.getGameState(), is(PlayerGameState.INITIALIZING));

    instance.updatePlayerGameState(PlayerGameState.LOBBY, player2);
    assertThat(player2.getCurrentGame(), is(game));
  }

  @Test
  public void joinGameWrongPassword() throws Exception {
    Game game = hostGame(player1);
    game.setPassword("pw");

    expectedException.expect(requestExceptionWithCode(ErrorCode.INVALID_PASSWORD));
    instance.joinGame(game.getId(), "PW", player2);
  }

  @Test
  public void updateGameStateIdle() {
    instance.createGame("Game title", FAF_TECHNICAL_NAME, MAP_NAME, "secret",
      GameVisibility.PUBLIC, GAME_MIN_RATING, GAME_MAX_RATING, player1);
    instance.updatePlayerGameState(PlayerGameState.IDLE, player1);

    Game game = instance.getActiveGame(1).get();

    assertThat(game.getState(), is(GameState.INITIALIZING));
  }

  @Test
  public void updateGameOption() throws Exception {
    Game game = hostGame(player1);
    assertThat(game.getOptions().containsKey("GameSpeed"), is(false));

    instance.updateGameOption(player1, "GameSpeed", "normal");

    assertThat(game.getOptions().get("GameSpeed"), is("normal"));
  }

  @Test
  public void updateGameOptionNotInGameIgnored() {
    instance.updateGameOption(player2, "GameSpeed", "normal");
    verifyZeroInteractions(clientService);
  }

  @Test
  public void updatePlayerOption() throws Exception {
    Game game = hostGame(player1);

    instance.updatePlayerOption(player1, player1.getId(), OPTION_FACTION, 1);
    assertThat(game.getPlayerOptions().get(player1.getId()).get(OPTION_FACTION), is(1));

    instance.updatePlayerOption(player1, player1.getId(), OPTION_FACTION, 2);
    assertThat(game.getPlayerOptions().get(player1.getId()).get(OPTION_FACTION), is(2));
  }

  @Test
  public void updateAiOptionArmy() throws Exception {
    Game game = hostGame(player1);
    assertThat(game.getAiOptions().containsKey(QAI), is(false));

    instance.updateAiOption(player1, QAI, OPTION_ARMY, 2);

    assertThat(game.getAiOptions().get(QAI).get(OPTION_ARMY), is(2));
  }

  @Test
  public void updateAiOptionFactionIgnored() throws Exception {
    Game game = hostGame(player1);
    assertThat(game.getAiOptions().containsKey(QAI), is(false));

    instance.updateAiOption(player1, QAI, OPTION_FACTION, 2);

    assertThat(game.getAiOptions().containsKey(QAI), is(false));
  }

  @Test
  public void clearSlot() throws Exception {
    Game game = hostGame(player1);
    instance.updatePlayerOption(game.getHost(), player1.getId(), OPTION_SLOT, 2);
    addPlayer(game, player2);

    instance.updatePlayerOption(player1, 1, OPTION_SLOT, 3);
    instance.updatePlayerOption(player1, 1, OPTION_FACTION, 3);
    instance.updatePlayerOption(player1, 2, OPTION_SLOT, 4);
    instance.updatePlayerOption(player1, 2, OPTION_FACTION, 4);

    instance.clearSlot(game, 1);
    assertThat(game.getPlayerOptions().containsKey(1), is(true));
    assertThat(game.getPlayerOptions().containsKey(2), is(true));

    instance.clearSlot(game, 4);
    assertThat(game.getPlayerOptions().containsKey(1), is(true));
    assertThat(game.getPlayerOptions().containsKey(2), is(false));

    instance.clearSlot(game, 3);
    assertThat(game.getPlayerOptions().containsKey(1), is(false));
    assertThat(game.getPlayerOptions().containsKey(2), is(false));

    instance.clearSlot(game, 2);
    assertThat(game.getPlayerOptions().containsKey(1), is(false));
    assertThat(game.getPlayerOptions().containsKey(2), is(false));
  }

  @Test
  public void reportDesync() throws Exception {
    Game game = hostGame(player1);
    assertThat(game.getDesyncCounter().intValue(), is(0));
    instance.reportDesync(player1);
    instance.reportDesync(player1);
    instance.reportDesync(player1);
    assertThat(game.getDesyncCounter().intValue(), is(3));
  }

  @Test
  public void updateGameMods() throws Exception {
    List<String> modUids = Arrays.asList("1-1-1-1", "2-2-2-2");
    when(modService.findModVersionsByUids(modUids)).thenReturn(Arrays.asList(
      new ModVersion().setUid("1-1-1-1").setMod(new Mod().setDisplayName("Mod #1")),
      new ModVersion().setUid("2-2-2-2").setMod(new Mod().setDisplayName("Mod #2"))
    ));

    Game game = hostGame(player1);
    instance.updateGameMods(game, modUids);

    List<ModVersion> simMods = game.getSimMods();
    assertThat(simMods, hasSize(2));
    assertThat(simMods.get(0).getUid(), is("1-1-1-1"));
    assertThat(simMods.get(0).getMod().getDisplayName(), is("Mod #1"));
    assertThat(simMods.get(1).getUid(), is("2-2-2-2"));
    assertThat(simMods.get(1).getMod().getDisplayName(), is("Mod #2"));
  }

  @Test
  public void updateGameModsCountClearsIfZero() throws Exception {
    List<String> modUids = Arrays.asList("1-1-1-1", "2-2-2-2");
    when(modService.findModVersionsByUids(modUids)).thenReturn(Arrays.asList(
      new ModVersion().setUid("1-1-1-1").setMod(new Mod().setDisplayName("Mod #1")),
      new ModVersion().setUid("2-2-2-2").setMod(new Mod().setDisplayName("Mod #2"))
    ));

    Game game = hostGame(player1);
    instance.updateGameMods(game, Arrays.asList("1-1-1-1", "2-2-2-2"));
    instance.updateGameModsCount(game, 0);

    assertThat(game.getSimMods(), is(empty()));
  }

  @Test
  public void updateGameModsCountDoesntClearIfNonZero() throws Exception {
    List<String> modUids = Arrays.asList("1-1-1-1", "2-2-2-2");
    when(modService.findModVersionsByUids(modUids)).thenReturn(Arrays.asList(
      new ModVersion().setUid("1-1-1-1").setMod(new Mod().setDisplayName("Mod #1")),
      new ModVersion().setUid("2-2-2-2").setMod(new Mod().setDisplayName("Mod #2"))
    ));

    Game game = hostGame(player1);
    instance.updateGameMods(game, Arrays.asList("1-1-1-1", "2-2-2-2"));
    instance.updateGameModsCount(game, 1);

    List<ModVersion> simMods = game.getSimMods();
    assertThat(simMods, hasSize(2));
    assertThat(simMods.get(0).getUid(), is("1-1-1-1"));
    assertThat(simMods.get(0).getMod().getDisplayName(), is("Mod #1"));
    assertThat(simMods.get(1).getUid(), is("2-2-2-2"));
    assertThat(simMods.get(1).getMod().getDisplayName(), is("Mod #2"));
  }

  @Test
  public void reportArmyScore() throws Exception {
    Game game = hostGame(player1);
    addPlayer(game, player2);

    instance.updatePlayerOption(player1, player1.getId(), OPTION_ARMY, 1);
    instance.updatePlayerOption(player1, player2.getId(), OPTION_ARMY, 2);

    instance.reportArmyScore(player1, 1, 10);
    instance.reportArmyScore(player1, 2, -1);
    instance.reportArmyScore(player2, 1, 10);
    instance.reportArmyScore(player2, 2, -1);

    assertThat(game.getReportedArmyResults().values(), hasSize(2));
    assertThat(game.getReportedArmyResults().get(player1.getId()).values(), hasSize(2));
    assertThat(game.getReportedArmyResults().get(player2.getId()).values(), hasSize(2));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void simpleTwoPlayerGame() throws Exception {
    Game game = hostGame(player1);
    addPlayer(game, player2);

    instance.updatePlayerOption(player1, player1.getId(), OPTION_ARMY, 1);
    instance.updatePlayerOption(player1, player2.getId(), OPTION_ARMY, 2);

    launchGame(game);

    instance.reportArmyScore(player1, 1, 10);
    instance.reportArmyScore(player1, 2, -1);

    instance.reportArmyOutcome(player1, 1, Outcome.VICTORY);
    instance.reportArmyOutcome(player1, 2, Outcome.DEFEAT);

    instance.reportArmyScore(player2, 1, 10);
    instance.reportArmyScore(player2, 2, -1);

    instance.reportArmyOutcome(player2, 1, Outcome.VICTORY);
    instance.reportArmyOutcome(player2, 2, Outcome.DEFEAT);

    instance.updatePlayerGameState(PlayerGameState.ENDED, player1);
    instance.updatePlayerGameState(PlayerGameState.ENDED, player2);

    instance.updatePlayerGameState(PlayerGameState.CLOSED, player1);
    verify(clientService).disconnectPlayerFromGame(eq(player1.getId()), (Collection<? extends ConnectionAware>) MockitoHamcrest.argThat(Matchers.contains(player2)));

    instance.updatePlayerGameState(PlayerGameState.CLOSED, player2);
    verify(clientService).disconnectPlayerFromGame(eq(player2.getId()), MockitoHamcrest.argThat(Matchers.empty()));

    assertThat(game.getPlayerStats().values(), hasSize(2));
    assertThat(game.getPlayerStats().get(player1.getId()).getScore(), is(10));
    assertThat(game.getPlayerStats().get(player2.getId()).getScore(), is(-1));
  }

  /**
   * Tests whether the service correctly chooses the scores reported by the majority of connected players and ignores
   * the results reported by a "cheater" (that is, someone who reports different results).
   */
  @Test
  public void reportArmyScoreWithCheater() throws Exception {
    Game game = hostGame(player1);
    Player player3 = (Player) new Player().setId(3);
    addPlayer(game, player2);
    addPlayer(game, player3);

    instance.updatePlayerOption(player1, player1.getId(), OPTION_ARMY, 1);
    instance.updatePlayerOption(player1, player2.getId(), OPTION_ARMY, 2);
    instance.updatePlayerOption(player1, player3.getId(), OPTION_ARMY, 3);

    launchGame(game);

    instance.reportArmyScore(player1, 1, 10);
    instance.reportArmyScore(player1, 2, -1);
    instance.reportArmyScore(player1, 3, -1);

    instance.reportArmyOutcome(player1, 1, Outcome.VICTORY);
    instance.reportArmyOutcome(player1, 2, Outcome.DEFEAT);
    instance.reportArmyOutcome(player1, 3, Outcome.DEFEAT);

    instance.reportArmyScore(player2, 1, 10);
    instance.reportArmyScore(player2, 2, -1);
    instance.reportArmyScore(player2, 3, -1);

    instance.reportArmyOutcome(player2, 1, Outcome.VICTORY);
    instance.reportArmyOutcome(player2, 2, Outcome.DEFEAT);
    instance.reportArmyOutcome(player2, 3, Outcome.DEFEAT);

    instance.reportArmyScore(player3, 1, -1);
    instance.reportArmyScore(player3, 2, -1);
    instance.reportArmyScore(player3, 3, 10);

    instance.reportArmyOutcome(player3, 1, Outcome.DEFEAT);
    instance.reportArmyOutcome(player3, 2, Outcome.DEFEAT);
    instance.reportArmyOutcome(player3, 3, Outcome.VICTORY);

    instance.updatePlayerGameState(PlayerGameState.ENDED, player1);
    instance.updatePlayerGameState(PlayerGameState.ENDED, player2);
    instance.updatePlayerGameState(PlayerGameState.ENDED, player3);

    assertThat(game.getPlayerStats().values(), hasSize(3));
    assertThat(game.getPlayerStats().get(player1.getId()).getScore(), is(10));
    assertThat(game.getPlayerStats().get(player2.getId()).getScore(), is(-1));
    assertThat(game.getPlayerStats().get(player3.getId()).getScore(), is(-1));
  }

  @Test
  public void reportArmyScoreAiScore() throws Exception {
    Game game = hostGame(player1);

    instance.updatePlayerOption(player1, player1.getId(), OPTION_ARMY, 1);
    instance.updateAiOption(player1, "QAI", OPTION_ARMY, 2);

    instance.reportArmyScore(player1, 1, 10);
    instance.reportArmyScore(player1, 2, -1);

    assertThat(game.getReportedArmyResults().values(), hasSize(1));
    assertThat(game.getReportedArmyResults().get(player1.getId()).values(), hasSize(2));
  }

  @Test
  public void reportArmyScoreNotInGame() throws Exception {
    Game game = hostGame(player1);
    assertThat(player2.getCurrentGame(), is(nullValue()));

    instance.reportArmyScore(player2, 1, 10);

    assertThat(game.getReportedArmyResults().values(), is(empty()));
  }

  @Test
  public void reportArmyOutcome() throws Exception {
    Game game = hostGame(player1);
    addPlayer(game, player2);

    instance.updatePlayerOption(player1, player1.getId(), OPTION_ARMY, 1);
    instance.updatePlayerOption(player1, player2.getId(), OPTION_ARMY, 2);

    launchGame(game);

    instance.reportArmyOutcome(player1, 1, Outcome.VICTORY);
    instance.reportArmyOutcome(player1, 2, Outcome.DEFEAT);
    instance.reportArmyOutcome(player2, 1, Outcome.VICTORY);
    instance.reportArmyOutcome(player2, 2, Outcome.DEFEAT);

    assertThat(game.getReportedArmyResults().values(), hasSize(2));
    assertThat(game.getReportedArmyResults().get(player1.getId()).values(), hasSize(2));
    assertThat(game.getReportedArmyResults().get(player2.getId()).values(), hasSize(2));
  }

  @Test
  public void reportArmyStatistics() throws Exception {
    Game game = hostGame(player1);

    assertThat(game.getArmyStatistics(), is(empty()));
    instance.reportArmyStatistics(player1, Arrays.asList(new ArmyStatistics(), new ArmyStatistics()));
    assertThat(game.getArmyStatistics(), is(notNullValue()));
    assertThat(game.getArmyStatistics(), hasSize(2));
  }

  @Test
  public void enforceRating() throws Exception {
    Game game = hostGame(player1);

    assertThat(game.isRatingEnforced(), is(false));
    instance.enforceRating(player1);
    assertThat(game.isRatingEnforced(), is(true));
  }

  @Test
  public void endGameIfNoPlayerConnected() throws Exception {
    Game game = hostGame(player1);

    assertThat(game.getState(), is(GameState.OPEN));

    addPlayer(game, player2);
    assertThat(game.getState(), is(GameState.OPEN));

    instance.updatePlayerGameState(PlayerGameState.CLOSED, player1);
    assertThat(game.getState(), is(GameState.OPEN));

    instance.updatePlayerGameState(PlayerGameState.CLOSED, player2);
    assertThat(game.getState(), is(GameState.CLOSED));

    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
    assertThat(player2.getCurrentGame(), is(nullValue()));
    assertThat(player2.getGameState(), is(PlayerGameState.NONE));
  }

  @Test
  public void onGameClosedDoesntSaveGameIfGameDidntStart() throws Exception {
    Game game = hostGame(player1);

    instance.updatePlayerGameState(PlayerGameState.CLOSED, player1);
    assertThat(game.getState(), is(GameState.CLOSED));

    verify(gameRepository, never()).save(any(Game.class));
    verifyZeroInteractions(divisionService);
    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
  }

  @Test
  public void onGameClosedDoesntProcessArmyStatsIfGameDidntStart() throws Exception {
    Game game = hostGame(player1);

    instance.updatePlayerGameState(PlayerGameState.CLOSED, player1);
    assertThat(game.getState(), is(GameState.CLOSED));

    verifyZeroInteractions(armyStatisticsService);
    verifyZeroInteractions(divisionService);
    verify(mapService, never()).incrementTimesPlayed(any());

    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
  }

  @Test
  public void onGameEndedSavesGameIfGameStarted() throws Exception {
    Game game = hostGame(player1);
    launchGame(game);

    instance.updatePlayerGameState(PlayerGameState.ENDED, player1);
    assertThat(game.getState(), is(GameState.ENDED));

    verify(gameRepository).save(game);
    verifyZeroInteractions(divisionService);
    assertThat(player1.getCurrentGame(), is(game));
    assertThat(player1.getGameState(), is(PlayerGameState.ENDED));
  }

  @Test
  public void onGameClosedSavesGameIfGameStarted() throws Exception {
    Game game = hostGame(player1);
    launchGame(game);

    instance.updatePlayerGameState(PlayerGameState.CLOSED, player1);
    assertThat(game.getState(), is(GameState.CLOSED));

    verify(gameRepository).save(game);
    verify(mapService).incrementTimesPlayed(game.getMapVersion().getMap());
    verifyZeroInteractions(divisionService);
    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
  }

  @Test
  public void onGameEndedProcessesStatsIfGameStarted() throws Exception {
    Game game = hostGame(player1);
    launchGame(game);

    instance.updatePlayerGameState(PlayerGameState.ENDED, player1);
    assertThat(game.getState(), is(GameState.ENDED));

    verify(armyStatisticsService).process(any(), eq(game), any());
    verifyZeroInteractions(divisionService);
    assertThat(player1.getCurrentGame(), is(game));
    assertThat(player1.getGameState(), is(PlayerGameState.ENDED));
  }

  @Test
  public void onGameClosedProcessesStatsIfGameStarted() throws Exception {
    Game game = hostGame(player1);
    launchGame(game);

    instance.updatePlayerGameState(PlayerGameState.CLOSED, player1);
    assertThat(game.getState(), is(GameState.CLOSED));

    verify(armyStatisticsService).process(any(), eq(game), any());
    verifyZeroInteractions(divisionService);
    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
  }

  @Test
  public void onLadder1v1GameEndedProcessedStats() throws Exception {
    when(modService.isLadder1v1(any())).thenReturn(true);
    player1.setLadder1v1Rating(mock(Ladder1v1Rating.class));
    player2.setLadder1v1Rating(mock(Ladder1v1Rating.class));

    Game game = hostGame(player1);
    addPlayer(game, player2);
    launchGame(game);

    reportPlayerScores();

    assertThat(game.getValidity(), is(Validity.VALID));
    verify(divisionService).postResult(player1, player2, player1);
  }

  @Test
  public void simpleValidGameWithEnded() throws Exception {
    Game game = hostGame(player1);
    addPlayer(game, player2);
    launchGame(game);

    reportPlayerScores();

    assertThat(game.getValidity(), is(Validity.VALID));
  }

  @Test
  public void onGameLaunching() throws Exception {
    Game game = hostGame(player1);

    instance.updatePlayerOption(player1, player1.getId(), OPTION_FACTION, 1);
    instance.updatePlayerGameState(PlayerGameState.LAUNCHING, player1);

    assertThat(game.getState(), is(GameState.PLAYING));
    assertThat(game.getStartTime(), is(lessThan(Instant.now().plusSeconds(1))));
    assertThat(game.getStartTime(), is(greaterThan(Instant.now().minusSeconds(10))));

    verify(entityManager).persist(game);
    verify(clientService, atLeastOnce()).broadcastDelayed(any(GameResponse.class), any(), any(), any(), any());
  }

  @Test
  public void onGameLaunchingSentByNonHostIsIgnored() throws Exception {
    Game game = hostGame(player1);

    addPlayer(game, player2);
    Mockito.reset(clientService);

    instance.updatePlayerGameState(PlayerGameState.LAUNCHING, player2);

    assertThat(game.getState(), is(GameState.OPEN));
    verifyZeroInteractions(clientService);
    verify(gameRepository, never()).save(game);
  }

  @Test
  public void updateGameValidityUnrankedMod() throws Exception {
    Game game = hostGame(player1);
    game.getSimMods().add(new ModVersion().setRanked(false).setMod(new Mod().setDisplayName("Mod")));

    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.BAD_MOD));
  }

  @Test
  public void updateGameValidityUnrankedMap() throws Exception {
    Game game = hostGame(player1);

    game.getMapVersion().setRanked(false);

    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.BAD_MAP));
  }

  @Test
  public void updateGameValidityWrongVictoryConditionDominationNotCoop() throws Exception {
    Game game = hostGame(player1);

    game.setVictoryCondition(VictoryCondition.DOMINATION);
    when(modService.isCoop(any())).thenReturn(false);

    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.WRONG_VICTORY_CONDITION));
  }

  @Test
  public void updateGameValidityWrongVictoryConditionEradicationNotCoop() throws Exception {
    Game game = hostGame(player1);

    game.setVictoryCondition(VictoryCondition.ERADICATION);
    when(modService.isCoop(any())).thenReturn(false);

    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.WRONG_VICTORY_CONDITION));
  }

  @Test
  public void updateGameValidityWrongVictoryConditionSandboxNotCoop() throws Exception {
    Game game = hostGame(player1);

    game.setVictoryCondition(VictoryCondition.SANDBOX);
    when(modService.isCoop(any())).thenReturn(false);

    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.WRONG_VICTORY_CONDITION));
  }

  @Test
  public void updateGameValidityUnitRestriction() throws Exception {
    Game game = hostGame(player1);

    addPlayer(game, player2);
    game.getOptions().put(OPTION_RESTRICTED_CATEGORIES, 1);

    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.BAD_UNIT_RESTRICTIONS));
  }

  @Test
  public void updateGameValidityFreeForAll() throws Exception {
    Player player3 = (Player) new Player().setId(3);

    Game game = hostGame(player1);
    addPlayer(game, player2);
    addPlayer(game, player3);

    instance.updatePlayerOption(player1, player1.getId(), OPTION_TEAM, 2);
    instance.updatePlayerOption(player1, player2.getId(), OPTION_TEAM, 3);
    instance.updatePlayerOption(player1, player3.getId(), OPTION_TEAM, 4);

    launchGame(game);

    game.getReportedArmyResults().put(1, Collections.emptyMap());
    game.getReportedArmyResults().put(2, Collections.emptyMap());
    game.getReportedArmyResults().put(1, Collections.emptyMap());
    game.getReportedArmyResults().put(2, Collections.emptyMap());

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.FREE_FOR_ALL));
  }

  @Test
  public void updateGameValidityUnevenTeams() throws Exception {
    Player player3 = (Player) new Player().setId(3);

    Game game = hostGame(player1);
    addPlayer(game, player2);
    addPlayer(game, player3);

    instance.updatePlayerOption(player1, player1.getId(), OPTION_TEAM, 2);
    instance.updatePlayerOption(player1, player2.getId(), OPTION_TEAM, 2);
    instance.updatePlayerOption(player1, player3.getId(), OPTION_TEAM, 3);

    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.UNEVEN_TEAMS));
  }

  /**
   * Tests that scores are calculated based on the reports of the majority of players that are still connected when the
   * game ends, even if there are kiddies trying to trick the system. The only case they are able to do so is if they
   * stay until the end of the game and share the majority of the result reports. The only way to prevent this would be
   * a neutral referee who can always be trusted.
   */
  @Test
  public void scoresCalculatedCorrectly() throws Exception {
    Player player3 = (Player) new Player().setId(3);
    Player player4 = (Player) new Player().setId(4);
    Player player5 = (Player) new Player().setId(5);
    Player player6 = (Player) new Player().setId(6);
    Player player7 = (Player) new Player().setId(7);
    Player player8 = (Player) new Player().setId(8);

    Game game = hostGame(player1);

    Stream.of(player2, player3, player4, player5, player6, player7, player8).forEach(
      player -> addPlayer(game, player));
    Stream.of(player1, player3, player5, player7).forEach(
      player -> instance.updatePlayerOption(player1, player.getId(), OPTION_TEAM, 2));
    Stream.of(player2, player4, player6, player8).forEach(
      player -> instance.updatePlayerOption(player1, player.getId(), OPTION_TEAM, 3));
    Stream.of(player1, player2, player3, player4, player5, player6, player7, player8).forEach(
      player -> instance.updatePlayerOption(player1, player.getId(), OPTION_ARMY, player.getId()));

    launchGame(game);

    // 6 of 8 players got killed, player 1 - 3 are the only ones who report the correct result
    Stream.of(player1, player2, player3)
      .forEach(reporter -> {
        // Army 3-8 have been defeated
        IntStream.range(3, 9)
          .forEach(armyId -> {
            instance.reportArmyOutcome(reporter, armyId, Outcome.DEFEAT);
            instance.reportArmyScore(reporter, armyId, -1);
          });

        // Army 1 and 2 scored and are still in game
        IntStream.range(1, 3)
          .forEach(armyId -> instance.reportArmyScore(reporter, armyId, 3));
      });

    // Players 4 - 8 are kiddies who try to trick the score system by reporting everybody but players 1 and 2 lost
    Stream.of(player4, player5, player6, player7, player8)
      .forEach(reporter -> {
        // Army 1 and 2 have allegedly been defeated
        IntStream.range(1, 3)
          .forEach(armyId -> {
            instance.reportArmyOutcome(reporter, armyId, Outcome.DEFEAT);
            instance.reportArmyScore(reporter, armyId, -1);
          });

        // Armies 3 - 8 allegedly won
        IntStream.range(3, 9)
          .forEach(armyId -> {
            instance.reportArmyOutcome(reporter, armyId, Outcome.VICTORY);
            instance.reportArmyScore(reporter, armyId, 10);
          });
      });

    // All but players 1, 2, and 3 disconnect
    Stream.of(player4, player5, player6, player7, player8)
      .forEach(player -> instance.removePlayer(player));

    // Player 1 claims to have defeated player 2, but player 2 and 3 claim that player 1 has been defeated.
    instance.reportArmyScore(player1, 1, 4);
    instance.reportArmyOutcome(player1, 1, Outcome.VICTORY);
    instance.reportArmyScore(player1, 2, 2);
    instance.reportArmyOutcome(player1, 2, Outcome.DEFEAT);

    Stream.of(player2, player3).forEach(reporter -> {
      instance.reportArmyScore(reporter, 1, 4);
      instance.reportArmyOutcome(reporter, 1, Outcome.DEFEAT);

      instance.reportArmyScore(reporter, 2, 2);
      instance.reportArmyOutcome(reporter, 2, Outcome.VICTORY);
    });

    /* Even though the intermediate results are the ones reported most often, the results to be considered are the ones
    with an outcome, since only those players saw the end of the game. */

    assertThat(game.getValidity(), is(Validity.VALID));
    // Expect that reports of all disconnected players have been discarded
    assertThat(game.getReportedArmyResults().values(), hasSize(3));
    // Expect all remaining players to have reported army results for all players
    game.getReportedArmyResults()
      .forEach((playerId, resultMap) -> assertThat("Player " + playerId + " did not report all results", resultMap.size(), is(8)));

    assertThat(game.getPlayerStats().get(1).getScore(), is(4));
    assertThat(game.getPlayerStats().get(2).getScore(), is(2));
    assertThat(game.getPlayerStats().get(3).getScore(), is(-1));
    assertThat(game.getPlayerStats().get(4).getScore(), is(-1));
    assertThat(game.getPlayerStats().get(5).getScore(), is(-1));
    assertThat(game.getPlayerStats().get(6).getScore(), is(-1));
    assertThat(game.getPlayerStats().get(7).getScore(), is(-1));
    assertThat(game.getPlayerStats().get(8).getScore(), is(-1));
  }

  @Test
  public void updateGameValidityTeamsUnlocked() throws Exception {
    Game game = hostGame(player1);
    addPlayer(game, player2);
    game.getOptions().put(GameService.OPTION_TEAM_LOCK, "unlocked");
    launchGame(game);

    reportPlayerScores();

    assertThat(game.getValidity(), is(Validity.TEAMS_UNLOCKED));
  }

  @Test
  public void simpleValidGameWithoutEnded() throws Exception {
    Game game = hostGame(player1);
    addPlayer(game, player2);
    launchGame(game);

    Stream.of(player1, player2).forEach(player -> {
      instance.reportArmyOutcome(player, 2, Outcome.DEFEAT);
      instance.reportArmyScore(player, 2, -1);
      instance.reportArmyOutcome(player, 1, Outcome.VICTORY);
      instance.reportArmyScore(player, 1, 10);
    });

    assertThat(game.getValidity(), is(Validity.VALID));
  }

  @Test
  public void updateGameValidityNoFogOfWar() throws Exception {
    Game game = hostGame(player1);
    game.getOptions().put(OPTION_FOG_OF_WAR, "foo");
    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.NO_FOG_OF_WAR));
  }

  @Test
  public void updateGameValidityCheatsEnabled() throws Exception {
    Game game = hostGame(player1);
    game.getOptions().put(OPTION_CHEATS_ENABLED, "true");

    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.CHEATS_ENABLED));
  }

  @Test
  public void updateGameValidityPrebuiltEnabled() throws Exception {
    Game game = hostGame(player1);
    game.getOptions().put(OPTION_PREBUILT_UNITS, "On");

    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.PREBUILT_ENABLED));
  }

  @Test
  public void updateGameValidityNoRushEnabled() throws Exception {
    Game game = hostGame(player1);
    game.getOptions().put(OPTION_NO_RUSH, "On");

    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.NO_RUSH_ENABLED));
  }

  @Test
  public void updateGameValidityTooManyDesyncs() throws Exception {
    Game game = hostGame(player1);
    game.getDesyncCounter().set(5);
    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.TOO_MANY_DESYNCS));
  }

  @Test
  public void updateGameValidityMutualDraw() throws Exception {
    Game game = hostGame(player1);
    game.setMutuallyAgreedDraw(true);

    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.MUTUAL_DRAW));
  }

  @Test
  public void updateGameValiditySinglePlayer() throws Exception {
    Game game = hostGame(player1);
    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.SINGLE_PLAYER));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void onAuthenticationSuccess() {
    player1.setCurrentGame(null);
    instance.createGame("Test game", FAF_TECHNICAL_NAME, MAP_NAME, null, GameVisibility.PUBLIC, GAME_MIN_RATING, GAME_MAX_RATING, player1);

    TestingAuthenticationToken authentication = new TestingAuthenticationToken("JUnit", "foo");
    authentication.setDetails(new TestingAuthenticationToken(new FafUserDetails((User) new User().setPlayer(player2).setPassword("pw").setLogin("JUnit")), null));

    instance.onPlayerOnlineEvent(new PlayerOnlineEvent(this, player2));

    ArgumentCaptor<GameResponses> captor = ArgumentCaptor.forClass((Class) Collection.class);
    verify(clientService).sendGameList(captor.capture(), eq(player2));
    GameResponses games = captor.getValue();

    assertThat(games.getResponses(), hasSize(1));
    assertThat(games.getResponses().iterator().next().getTitle(), is("Test game"));
  }

  @Test
  public void updateGameValidityUnknownResult() throws Exception {
    Game game = hostGame(player1);
    addPlayer(game, player2);
    launchGame(game);

    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.UNKNOWN_RESULT));
  }

  @Test
  public void updateGameValidityTooShort() throws Exception {
    Game game = hostGame(player1);
    addPlayer(game, player2);

    launchGame(game);

    game.getReportedArmyResults().put(1, ImmutableMap.of(1, ArmyResult.of(1, Outcome.VICTORY, 10)));

    serverProperties.getGame().setRankedMinTimeMultiplicator(10_000);
    instance.updateGameValidity(game);

    assertThat(game.getValidity(), is(Validity.TOO_SHORT));
  }

  @Test(expected = IllegalStateException.class)
  public void updateGameValidityAlreadySetThrowsException() throws Exception {
    Game game = hostGame(player1);
    game.setValidity(Validity.UNKNOWN_RESULT);
    instance.updateGameValidity(game);
  }

  @Test
  public void onClientDisconnectRemovesPlayerAndUnsetsGameAndRemovesGameIfLastPlayer() throws Exception {
    Game game = hostGame(player1);

    assertThat(player1.getCurrentGame(), is(game));
    assertThat(player1.getGameState(), is(PlayerGameState.LOBBY));
    assertThat(instance.getActiveGame(game.getId()).isPresent(), is(true));

    instance.removePlayer(player1);

    assertThat(player1.getCurrentGame(), is(nullValue()));
    assertThat(player1.getGameState(), is(PlayerGameState.NONE));
    assertThat(instance.getActiveGame(game.getId()).isPresent(), is(false));
  }

  /**
   * Tests whether all but the affected player are informed to drop someone.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void disconnectFromGame() throws Exception {
    Game game = hostGame(player1);

    Player player3 = (Player) new Player().setId(3);
    Player player4 = (Player) new Player().setId(4);

    addPlayer(game, player2);
    addPlayer(game, player3);
    addPlayer(game, player4);

    when(playerService.getOnlinePlayer(3)).thenReturn(Optional.of(player3));

    instance.disconnectPlayerFromGame(player1, 3);

    ArgumentCaptor<List<ConnectionAware>> captor = ArgumentCaptor.forClass((Class) List.class);
    verify(clientService).disconnectPlayerFromGame(eq(3), captor.capture());
    List<ConnectionAware> recipients = captor.getValue();

    assertThat(recipients, hasSize(3));
    assertThat(recipients, hasItems(
      player1, player2, player4
    ));
  }

  @Test
  public void disconnectFromGameIgnoredWhenPlayerUnknown() {
    instance.disconnectPlayerFromGame(player1, 412312);
    verifyZeroInteractions(clientService);
  }

  @Test
  public void disconnectFromGameIgnoredWhenPlayerNotInGame() {
    when(playerService.getOnlinePlayer(3)).thenReturn(Optional.of(new Player()));
    instance.disconnectPlayerFromGame(player1, 3);
    verifyZeroInteractions(clientService);
  }

  @Test
  public void mutualDrawRequestedByPlayerInNonPlayingGameState() {
    player1.setCurrentGame(null);
    instance.createGame("Game title", FAF_TECHNICAL_NAME, MAP_NAME, "secret", GameVisibility.PUBLIC, GAME_MIN_RATING, GAME_MAX_RATING, player1);
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player1);

    expectedException.expect(requestExceptionWithCode(ErrorCode.INVALID_GAME_STATE));

    instance.mutuallyAgreeDraw(player1);
  }

  @Test
  public void restoreGameSessionWasNeverInGame() throws Exception {
    Game game = hostGame(player1);
    launchGame(game);

    expectedException.expect(requestExceptionWithCode(ErrorCode.CANT_RESTORE_GAME_NOT_PARTICIPANT));

    instance.restoreGameSession(player2, game.getId());
  }

  @Test
  public void restoreGameSessionOpenGame() throws Exception {
    Game game = hostGame(player1);

    assertThat(player2.getCurrentGame(), is(nullValue()));

    instance.joinGame(game.getId(), game.getPassword(), player2);
    assertThat(player2.getCurrentGame(), is(game));
    assertThat(player2.getGameState(), is(PlayerGameState.INITIALIZING));

    instance.updatePlayerGameState(PlayerGameState.LOBBY, player2);
    instance.updatePlayerGameState(PlayerGameState.LAUNCHING, player2);
    assertThat(player2.getGameState(), is(PlayerGameState.LAUNCHING));

    ClientConnection clientConnection = new ClientConnection("1", Protocol.V1_LEGACY_UTF_16, mock(InetAddress.class));
    clientConnection.setAuthentication(new TestingAuthenticationToken(new FafUserDetails((User) new User().setPlayer(player2).setPassword("pw").setLogin("JUnit")), null));
    player2.setClientConnection(clientConnection);
    instance.removePlayer(player2);
    assertThat(player2.getCurrentGame(), is(nullValue()));
    assertThat(player2.getGameState(), is(PlayerGameState.NONE));

    instance.restoreGameSession(player2, game.getId());
    assertThat(player2.getCurrentGame(), is(game));
    assertThat(player2.getGameState(), is(PlayerGameState.LOBBY));
  }

  @Test
  public void restoreGameSessionPlayingGame() throws Exception {
    Game game = hostGame(player1);

    instance.joinGame(game.getId(), game.getPassword(), player2);
    assertThat(player2.getCurrentGame(), is(game));

    instance.updatePlayerGameState(PlayerGameState.LOBBY, player2);
    instance.updatePlayerGameState(PlayerGameState.LAUNCHING, player2);
    assertThat(player2.getGameState(), is(PlayerGameState.LAUNCHING));

    instance.updatePlayerGameState(PlayerGameState.LAUNCHING, player1);

    ClientConnection clientConnection = new ClientConnection("1", Protocol.V1_LEGACY_UTF_16, mock(InetAddress.class));
    clientConnection.setAuthentication(new TestingAuthenticationToken(new FafUserDetails((User) new User().setPlayer(player2).setPassword("pw").setLogin("JUnit")), null));
    player2.setClientConnection(clientConnection);
    instance.removePlayer(player2);
    assertThat(player2.getCurrentGame(), is(nullValue()));
    assertThat(player2.getGameState(), is(PlayerGameState.NONE));

    instance.restoreGameSession(player2, game.getId());
    assertThat(player2.getCurrentGame(), is(game));
    assertThat(player2.getGameState(), is(PlayerGameState.LAUNCHING));
  }

  @Test
  public void mutualDrawRequestedByPlayerWithoutGame() throws Exception {
    Game game = hostGame(player1);
    launchGame(game);

    expectedException.expect(requestExceptionWithCode(ErrorCode.NOT_IN_A_GAME));

    instance.mutuallyAgreeDraw(player2);
  }

  @Test
  public void mutualDrawRequestedByObserver() throws Exception {
    Game game = hostGame(player1);
    instance.updatePlayerOption(player1, player1.getId(), OPTION_TEAM, GameService.OBSERVERS_TEAM_ID);
    launchGame(game);

    instance.mutuallyAgreeDraw(player1);

    assertThat(game.isMutuallyAgreedDraw(), is(false));
  }

  @Test
  public void mutualDrawRequestedByPlayer() throws Exception {
    Game game = hostGame(player1);
    instance.updatePlayerOption(player1, player1.getId(), OPTION_TEAM, GameService.NO_TEAM_ID);
    launchGame(game);

    instance.mutuallyAgreeDraw(player1);

    assertThat(game.isMutuallyAgreedDraw(), is(true));
  }

  @Test
  public void mutualDrawRequestedByAllPlayers() throws Exception {
    Game game = hostGame(player1);
    instance.updatePlayerOption(player1, player1.getId(), OPTION_TEAM, 2);

    instance.joinGame(game.getId(), game.getPassword(), player2);
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player2);
    instance.updatePlayerOption(player1, player2.getId(), OPTION_TEAM, 3);

    Player player3 = new Player();
    player3.setId(3);

    instance.joinGame(game.getId(), game.getPassword(), player3);
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player3);
    instance.updatePlayerOption(player1, player3.getId(), OPTION_TEAM, GameService.OBSERVERS_TEAM_ID);

    launchGame(game);

    instance.mutuallyAgreeDraw(player1);

    assertThat(game.isMutuallyAgreedDraw(), is(false));

    instance.mutuallyAgreeDraw(player2);

    assertThat(game.isMutuallyAgreedDraw(), is(true));
  }

  private void reportPlayerScores() {
    Stream.of(player1, player2)
      .forEach(player -> {
        instance.reportArmyOutcome(player, 1, Outcome.VICTORY);
        instance.reportArmyScore(player, 1, 10);
        instance.reportArmyOutcome(player, 2, Outcome.DEFEAT);
        instance.reportArmyScore(player, 2, -1);
      });
  }

  private Game hostGame(Player host) throws Exception {
    player1.setCurrentGame(null);

    CompletableFuture<Game> joinable = instance.createGame("Game title", FAF_TECHNICAL_NAME, MAP_NAME, "secret",
      GameVisibility.PUBLIC, GAME_MIN_RATING, GAME_MAX_RATING, host);

    assertThat(joinable.isDone(), is(false));
    assertThat(joinable.isCancelled(), is(false));
    assertThat(joinable.isCompletedExceptionally(), is(false));

    assertThat(meterRegistry.find(Metrics.GAMES).tag(GameService.TAG_GAME_STATE, "").gauge().value(), is(1d));
    assertThat(meterRegistry.find(Metrics.GAMES).tag(GameService.TAG_GAME_STATE, GameState.INITIALIZING.name()).gauge().value(), is(1d));

    Game game = instance.getActiveGame(1).get();
    assertThat(game.getState(), is(GameState.INITIALIZING));
    assertThat(player1.getCurrentGame(), is(game));
    assertThat(player1.getGameState(), is(PlayerGameState.INITIALIZING));

    instance.updatePlayerGameState(PlayerGameState.LOBBY, player1);
    assertThat(player1.getGameState(), is(PlayerGameState.LOBBY));

    game = joinable.get();

    instance.updateGameOption(host, OPTION_VICTORY_CONDITION, VictoryCondition.DEMORALIZATION.getString());
    instance.updateGameOption(host, OPTION_FOG_OF_WAR, "explored");
    instance.updateGameOption(host, OPTION_CHEATS_ENABLED, "false");
    instance.updateGameOption(host, OPTION_PREBUILT_UNITS, "Off");
    instance.updateGameOption(host, OPTION_NO_RUSH, "Off");
    instance.updateGameOption(host, OPTION_RESTRICTED_CATEGORIES, 0);

    instance.updatePlayerOption(host, host.getId(), OPTION_ARMY, host.getId());
    instance.updatePlayerOption(host, host.getId(), OPTION_FACTION, 1);
    instance.updatePlayerOption(host, host.getId(), OPTION_COLOR, host.getId());
    instance.updatePlayerOption(host, host.getId(), OPTION_START_SPOT, host.getId());
    instance.updatePlayerOption(host, host.getId(), OPTION_TEAM, GameService.NO_TEAM_ID);

    assertThat(meterRegistry.find(Metrics.GAMES).gauge().value(), is(1d));
    assertThat(meterRegistry.find(Metrics.GAMES).tag(GameService.TAG_GAME_STATE, GameState.OPEN.name()).gauge().value(), is(1d));

    verify(clientService).startGameProcess(game, player1);
    assertThat(game.getTitle(), is("Game title"));
    assertThat(game.getHost(), is(player1));
    assertThat(game.getFeaturedMod().getTechnicalName(), is(FAF_TECHNICAL_NAME));
    assertThat(game.getMapVersion(), is(notNullValue()));
    assertThat(game.getMapName(), is(MAP_NAME));
    assertThat(game.getPassword(), is("secret"));
    assertThat(game.getState(), is(GameState.OPEN));
    assertThat(game.getGameVisibility(), is(GameVisibility.PUBLIC));
    assertThat(game.getMinRating(), is(GAME_MIN_RATING));
    assertThat(game.getMaxRating(), is(GAME_MAX_RATING));
    assertThat(player1.getCurrentGame(), is(game));

    return game;
  }

  private void launchGame(Game game) {
    game.getConnectedPlayers().values()
      .forEach(player -> instance.updatePlayerGameState(PlayerGameState.LAUNCHING, player));

    assertThat(meterRegistry.find(Metrics.GAMES).gauge().value(), is(1d));
    assertThat(meterRegistry.find(Metrics.GAMES).tag(GameService.TAG_GAME_STATE, GameState.PLAYING.name()).gauge().value(), is(1d));
  }

  private void addPlayer(Game game, Player player) {
    instance.joinGame(game.getId(), game.getPassword(), player);
    instance.updatePlayerGameState(PlayerGameState.LOBBY, player);

    Player host = game.getHost();
    instance.updatePlayerOption(host, player.getId(), OPTION_FACTION, 1);
    instance.updatePlayerOption(host, player.getId(), OPTION_SLOT, player.getId());
    instance.updatePlayerOption(host, player.getId(), OPTION_ARMY, player.getId());
    instance.updatePlayerOption(host, player.getId(), OPTION_COLOR, player.getId());
    instance.updatePlayerOption(host, player.getId(), OPTION_START_SPOT, player.getId());
    instance.updatePlayerOption(host, player.getId(), OPTION_TEAM, GameService.NO_TEAM_ID);
  }
}
