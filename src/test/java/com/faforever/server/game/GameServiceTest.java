package com.faforever.server.game;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.Player;
import com.faforever.server.map.MapService;
import com.faforever.server.mod.ModService;
import com.faforever.server.rating.RatingService;
import com.faforever.server.statistics.ArmyStatistics;
import com.faforever.server.stats.ArmyStatisticsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
// TODO more testing needed
public class GameServiceTest {

  private static final String PLAYER_NAME_1 = "player1";
  private static final String PLAYER_NAME_2 = "player2";
  private static final String MAP_NAME = "SCMP_001";
  private static final byte FAF_MOD_ID = 1;
  private static final int NEXT_GAME_ID = 1;

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

  private Player player1;
  private Player player2;
  private Game game;

  @Before
  public void setUp() throws Exception {
    game = new Game();
    game.setId(1);

    player1 = new Player();
    player1.setId(1);
    player1.setLogin(PLAYER_NAME_1);
    player1.setCurrentGame(game);
    game.setHost(player1);

    player2 = new Player();
    player2.setId(2);
    player2.setLogin(PLAYER_NAME_2);

    FeaturedMod fafFeaturedMod = new FeaturedMod();
    fafFeaturedMod.setId(FAF_MOD_ID);

    when(gameRepository.findMaxId()).thenReturn(Optional.of(NEXT_GAME_ID));
    when(mapService.findMap(anyString())).thenReturn(Optional.empty());
    when(modService.getFeaturedMod(FAF_MOD_ID)).thenReturn(Optional.of(fafFeaturedMod));

    instance = new GameService(gameRepository, clientService, mapService, modService, ratingService, armyStatisticsService);
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
    assertThat(game.getGameState(), is(nullValue()));
    assertThat(game.getGameVisibility(), is(GameVisibility.PUBLIC));
    assertThat(player1.getCurrentGame(), is(game));
  }

  @Test
  public void joinGame() throws Exception {
    player1.setCurrentGame(null);
    instance.createGame("Test game", FAF_MOD_ID, MAP_NAME, null, GameVisibility.PUBLIC, player1);
    instance.joinGame(NEXT_GAME_ID, player2);

    Optional<Game> optional = instance.getGame(NEXT_GAME_ID);
    assertThat(optional.isPresent(), is(true));
    Game game = optional.get();

    verify(clientService).startGameProcess(game, player1);
    assertThat(player1.getCurrentGame(), is(game));
  }

  @Test
  public void updateGameStateIdle() throws Exception {
    instance.updateGameState(GameState.LOBBY, player1);

    assertThat(game.getGameState(), is(GameState.LOBBY));
  }

  @Test
  public void updateGameOption() throws Exception {
    assertThat(game.getOptions().containsKey("GameSpeed"), is(false));

    instance.updateGameOption(player1, "GameSpeed", "normal");

    assertThat(game.getOptions().get("GameSpeed"), is("normal"));
  }

  @Test
  public void updatePlayerOption() throws Exception {
    assertThat(game.getPlayerOptions().containsKey(player1.getId()), is(false));

    instance.updatePlayerOption(player1, player1.getId(), "Faction", 1);

    assertThat(game.getPlayerOptions().get(player1.getId()).get("Faction"), is(1));
  }

  @Test
  public void updateAiOption() throws Exception {
    assertThat(game.getAiOptions().containsKey("QAI"), is(false));

    instance.updateAiOption(player1, "QAI", "Faction", 2);

    assertThat(game.getAiOptions().get("QAI").get("Faction"), is(2));
  }

  @Test
  public void clearSlot() throws Exception {
    instance.clearSlot(game, 1);
    // Since this message is ignored, there's nothing to assert
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
    instance.updatePlayerOption(player1, player1.getId(), "Army", 1);
    instance.updatePlayerOption(player1, player2.getId(), "Army", 2);

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
    instance.updatePlayerOption(player1, player1.getId(), "Army", 1);
    instance.updatePlayerOption(player1, player2.getId(), "Army", 2);

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
}
