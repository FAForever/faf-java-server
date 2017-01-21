package com.faforever.server.stats;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.ArmyOutcome;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.Player;
import com.faforever.server.game.Faction;
import com.faforever.server.game.Outcome;
import com.faforever.server.mod.ModService;
import com.faforever.server.stats.achievements.AchievementId;
import com.faforever.server.stats.achievements.AchievementService;
import com.faforever.server.stats.achievements.AchievementUpdate;
import com.faforever.server.stats.event.EventId;
import com.faforever.server.stats.event.EventService;
import com.faforever.server.stats.event.EventUpdate;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArmyStatisticsServiceTest {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private EventService eventService;
  @Mock
  private AchievementService achievementService;
  @Mock
  private ClientService clientService;
  @Mock
  private ModService modService;

  private ArmyStatisticsService instance;
  private Player player;
  private Game game;
  private ArmyStatistics.CategoryStats unitStats;
  private ArrayList<AchievementUpdate> achievementUpdates;
  private ArrayList<EventUpdate> eventUpdates;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    instance = new ArmyStatisticsService(achievementService, eventService, clientService, modService);

    player = new Player();
    player.setLogin("TestUser");
    player.setId(42);

    game = new Game();
    game.setId(1);

    unitStats = new ArmyStatistics.CategoryStats() {{
      setAir(new ArmyStatistics.UnitStats());
      setLand(new ArmyStatistics.UnitStats());
      setNaval(new ArmyStatistics.UnitStats());
      setExperimental(new ArmyStatistics.UnitStats());
      setCdr(new ArmyStatistics.UnitStats());
      setTech1(new ArmyStatistics.UnitStats());
      setTech2(new ArmyStatistics.UnitStats());
      setTech3(new ArmyStatistics.UnitStats());
      setEngineer(new ArmyStatistics.UnitStats());
      setTransportation(new ArmyStatistics.UnitStats());
      setSacu(new ArmyStatistics.UnitStats());
    }};
    achievementUpdates = new ArrayList<>();
    eventUpdates = new ArrayList<>();

    doAnswer(invocation -> {
      achievementUpdates.addAll((Collection<? extends AchievementUpdate>) invocation.getArguments()[1]);
      return CompletableFuture.completedFuture(null);
    }).when(achievementService).executeBatchUpdate(any(), any());

    doAnswer(invocation -> {
      eventUpdates.addAll((Collection<? extends EventUpdate>) invocation.getArguments()[1]);
      return CompletableFuture.completedFuture(null);
    }).when(eventService).executeBatchUpdate(any(), any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testProcess() throws Exception {
    String file = "/stats/game_stats_full_example.json";
    List<ArmyStatistics> stats = readStats(file);

    game.getReportedArmyOutcomes().put(player.getId(), singletonList(new ArmyOutcome(1, Outcome.VICTORY)));

    instance.process(player, game, stats);

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_NOVICE, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_JUNIOR, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_SENIOR, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_VETERAN, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_ADDICT, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_THAAM, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_YENZYNE, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_SUTHANUS, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_DONT_MESS_WITH_ME, AchievementUpdate.UpdateType.INCREMENT, 3)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_NO_MERCY, AchievementUpdate.UpdateType.INCREMENT, 154)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_DEADLY_BUGS, AchievementUpdate.UpdateType.INCREMENT, 147)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_IT_AINT_A_CITY, AchievementUpdate.UpdateType.INCREMENT, 47)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_STORMY_SEA, AchievementUpdate.UpdateType.INCREMENT, 74)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_LANDLUBBER, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_SEAMAN, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_ADMIRAL_OF_THE_FLEET, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_DEATH_FROM_ABOVE, AchievementUpdate.UpdateType.INCREMENT, 71)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_ASS_WASHER, AchievementUpdate.UpdateType.INCREMENT, 37)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_ALIEN_INVASION, AchievementUpdate.UpdateType.INCREMENT, 41)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_FATTER_IS_BETTER, AchievementUpdate.UpdateType.INCREMENT, 73)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_ARACHNOLOGIST, AchievementUpdate.UpdateType.INCREMENT, 87)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_INCOMING_ROBOTS, AchievementUpdate.UpdateType.INCREMENT, 83)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_FLYING_DEATH, AchievementUpdate.UpdateType.INCREMENT, 49)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_HOLY_CRAB, AchievementUpdate.UpdateType.INCREMENT, 51)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_THE_TRANSPORTER, AchievementUpdate.UpdateType.INCREMENT, 101)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_DR_EVIL, AchievementUpdate.UpdateType.INCREMENT, 20)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_TECHIE, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_I_LOVE_BIG_TOYS, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_EXPERIMENTALIST, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_WHO_NEEDS_SUPPORT, AchievementUpdate.UpdateType.SET_STEPS_AT_LEAST, 110)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_WHAT_A_SWARM, AchievementUpdate.UpdateType.SET_STEPS_AT_LEAST, 198)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_HATTRICK, AchievementUpdate.UpdateType.UNLOCK, 0)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_RAINMAKER, AchievementUpdate.UpdateType.UNLOCK, 0)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_NUCLEAR_WAR, AchievementUpdate.UpdateType.UNLOCK, 0)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_SO_MUCH_RESOURCES, AchievementUpdate.UpdateType.UNLOCK, 0)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_MAKE_IT_HAIL, AchievementUpdate.UpdateType.UNLOCK, 0)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_I_HAVE_A_CANON, AchievementUpdate.UpdateType.UNLOCK, 0)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_THAT_WAS_CLOSE, AchievementUpdate.UpdateType.UNLOCK, 0)));

    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_LOST_ACUS, 0)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_BUILT_AIR_UNITS, 1)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_LOST_AIR_UNITS, 2)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_BUILT_LAND_UNITS, 4)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_LOST_LAND_UNITS, 5)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_BUILT_NAVAL_UNITS, 33)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_LOST_NAVAL_UNITS, 11)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_LOST_TECH_1_UNITS, 12)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_LOST_TECH_2_UNITS, 13)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_LOST_TECH_3_UNITS, 14)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_BUILT_TECH_1_UNITS, 16)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_BUILT_TECH_2_UNITS, 17)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_BUILT_TECH_3_UNITS, 18)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_LOST_EXPERIMENTALS, 19)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_BUILT_EXPERIMENTALS, 20)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_LOST_ENGINEERS, 21)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_BUILT_ENGINEERS, 22)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_SERAPHIM_PLAYS, 1)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_SERAPHIM_WINS, 1)));

    verify(achievementService).executeBatchUpdate(player, achievementUpdates);
    verify(eventService).executeBatchUpdate(player, eventUpdates);
  }

  @Test
  public void testProcessSinglePlayer() throws Exception {
    List<ArmyStatistics> stats = readStats("/stats/game_stats_single_player.json");

    instance.process(player, game, stats);
    verifyZeroInteractions(achievementService);
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testProcessAiGame() throws Exception {
    List<ArmyStatistics> stats = readStats("/stats/game_stats_ai_game.json");

    instance.process(player, game, stats);
    verifyZeroInteractions(achievementService);
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testProcessGameWonLadder1v1() throws Exception {
    FeaturedMod ladder1v1FeaturedMod = new FeaturedMod();
    when(modService.isLadder1v1(ladder1v1FeaturedMod)).thenReturn(true);

    game.setFeaturedMod(ladder1v1FeaturedMod);
    game.getReportedArmyOutcomes().put(player.getId(), singletonList(new ArmyOutcome(1, Outcome.VICTORY)));

    List<ArmyStatistics> stats = readStats("/stats/game_stats_simple_win.json");

    instance.process(player, game, stats);

    assertThat(this.achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_FIRST_SUCCESS, AchievementUpdate.UpdateType.UNLOCK, 0)));
  }

  @Test
  public void testCategoryStatsWonMoreAir() throws Exception {
    unitStats.getAir().setBuilt(3);
    unitStats.getLand().setBuilt(2);
    unitStats.getNaval().setBuilt(1);

    instance.categoryStats(unitStats, true, achievementUpdates, eventUpdates);

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_WRIGHT_BROTHER, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_WINGMAN, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_KING_OF_THE_SKIES, AchievementUpdate.UpdateType.INCREMENT, 1)));
  }

  @Test
  public void testCategoryStatsWonMoreLand() throws Exception {
    unitStats.getAir().setBuilt(2);
    unitStats.getLand().setBuilt(3);
    unitStats.getNaval().setBuilt(1);

    instance.categoryStats(unitStats, true, achievementUpdates, eventUpdates);

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_MILITIAMAN, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_GRENADIER, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_FIELD_MARSHAL, AchievementUpdate.UpdateType.INCREMENT, 1)));
  }

  @Test
  public void testCategoryStatsWonMoreNaval() throws Exception {
    unitStats.getAir().setBuilt(2);
    unitStats.getLand().setBuilt(1);
    unitStats.getNaval().setBuilt(3);

    instance.categoryStats(unitStats, true, achievementUpdates, eventUpdates);

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_LANDLUBBER, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_SEAMAN, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_ADMIRAL_OF_THE_FLEET, AchievementUpdate.UpdateType.INCREMENT, 1)));
  }

  @Test
  public void testCategoryStatsWonMoreNavalAndOneExperimental() throws Exception {
    unitStats.getAir().setBuilt(2);
    unitStats.getLand().setBuilt(1);
    unitStats.getNaval().setBuilt(3);
    unitStats.getExperimental().setBuilt(1);

    instance.categoryStats(unitStats, true, achievementUpdates, eventUpdates);

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_LANDLUBBER, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_SEAMAN, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_ADMIRAL_OF_THE_FLEET, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_DR_EVIL, AchievementUpdate.UpdateType.INCREMENT, 1)));
  }

  @Test
  public void testCategoryStatsWonMoreNavalAndThreeExperimentals() throws Exception {
    unitStats.getAir().setBuilt(2);
    unitStats.getLand().setBuilt(1);
    unitStats.getNaval().setBuilt(3);
    unitStats.getExperimental().setBuilt(3);

    instance.categoryStats(unitStats, true, achievementUpdates, eventUpdates);

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_LANDLUBBER, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_SEAMAN, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_ADMIRAL_OF_THE_FLEET, AchievementUpdate.UpdateType.INCREMENT, 1)));

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_DR_EVIL, AchievementUpdate.UpdateType.INCREMENT, 3)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_TECHIE, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_I_LOVE_BIG_TOYS, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_EXPERIMENTALIST, AchievementUpdate.UpdateType.INCREMENT, 1)));
  }

  @Test
  public void testFactionPlayedAeonSurvived() throws Exception {
    instance.factionPlayed(Faction.AEON, true, achievementUpdates, eventUpdates);

    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_AEON_PLAYS, 1)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_AEON_WINS, 1)));

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_AURORA, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_BLAZE, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_SERENITY, AchievementUpdate.UpdateType.INCREMENT, 1)));
  }

  @Test
  public void testFactionPlayedAeonDied() throws Exception {
    instance.factionPlayed(Faction.AEON, false, achievementUpdates, eventUpdates);

    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_AEON_PLAYS, 1)));
  }

  @Test
  public void testFactionPlayedCybranSurvived() throws Exception {
    instance.factionPlayed(Faction.CYBRAN, true, achievementUpdates, eventUpdates);

    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_CYBRAN_PLAYS, 1)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_CYBRAN_WINS, 1)));

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_MANTIS, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_WAGNER, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_TREBUCHET, AchievementUpdate.UpdateType.INCREMENT, 1)));
  }

  @Test
  public void testFactionPlayedCybranDied() throws Exception {
    instance.factionPlayed(Faction.CYBRAN, false, achievementUpdates, eventUpdates);

    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_CYBRAN_PLAYS, 1)));
  }

  @Test
  public void testFactionPlayedUefSurvived() throws Exception {
    instance.factionPlayed(Faction.UEF, true, achievementUpdates, eventUpdates);

    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_UEF_PLAYS, 1)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_UEF_WINS, 1)));

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_MA12_STRIKER, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_RIPTIDE, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_DEMOLISHER, AchievementUpdate.UpdateType.INCREMENT, 1)));
  }

  @Test
  public void testFactionPlayedUefDied() throws Exception {
    instance.factionPlayed(Faction.UEF, false, achievementUpdates, eventUpdates);

    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_UEF_PLAYS, 1)));
  }

  @Test
  public void testFactionPlayedSeraphimSurvived() throws Exception {
    instance.factionPlayed(Faction.SERAPHIM, true, achievementUpdates, eventUpdates);

    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_SERAPHIM_PLAYS, 1)));
    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_SERAPHIM_WINS, 1)));

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_THAAM, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_YENZYNE, AchievementUpdate.UpdateType.INCREMENT, 1)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_SUTHANUS, AchievementUpdate.UpdateType.INCREMENT, 1)));
  }

  @Test
  public void testFactionPlayedSeraphimDied() throws Exception {
    instance.factionPlayed(Faction.SERAPHIM, false, achievementUpdates, eventUpdates);

    assertThat(eventUpdates, hasItem(new EventUpdate(EventId.EVENT_SERAPHIM_PLAYS, 1)));
    verifyZeroInteractions(achievementService);
  }

  @Test
  public void testKilledAcusOneAndSurvived() throws Exception {
    unitStats.getCdr().setKilled(1);
    instance.killedAcus(unitStats, true, achievementUpdates);

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_DONT_MESS_WITH_ME, AchievementUpdate.UpdateType.INCREMENT, 1)));
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testKilledAcusThreeAndSurvived() throws Exception {
    unitStats.getCdr().setKilled(3);
    instance.killedAcus(unitStats, true, achievementUpdates);

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_DONT_MESS_WITH_ME, AchievementUpdate.UpdateType.INCREMENT, 3)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_HATTRICK, AchievementUpdate.UpdateType.UNLOCK, 0)));
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testKilledAcusOneAndDied() throws Exception {
    unitStats.getCdr().setKilled(1);
    unitStats.getCdr().setLost(1);
    instance.killedAcus(unitStats, false, achievementUpdates);

    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_DONT_MESS_WITH_ME, AchievementUpdate.UpdateType.INCREMENT, 1)));
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testBuiltSalvationsOneAndDied() throws Exception {
    instance.builtSalvations(1, false, achievementUpdates);
    verifyZeroInteractions(achievementService);
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testBuiltSalvationsOneAndSurvived() throws Exception {
    instance.builtSalvations(1, true, achievementUpdates);
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_RAINMAKER, AchievementUpdate.UpdateType.UNLOCK, 0)));
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testBuiltYolonaOssOneAndDied() throws Exception {
    instance.builtYolonaOss(1, false, achievementUpdates);
    verifyZeroInteractions(achievementService);
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testBuiltYolonaOssOneAndSurvived() throws Exception {
    instance.builtYolonaOss(1, true, achievementUpdates);
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_NUCLEAR_WAR, AchievementUpdate.UpdateType.UNLOCK, 0)));
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testBuiltParagonsOneAndDied() throws Exception {
    instance.builtParagons(1, false, achievementUpdates);
    verifyZeroInteractions(achievementService);
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testBuiltParagonsOneAndSurvived() throws Exception {
    instance.builtParagons(1, true, achievementUpdates);
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_SO_MUCH_RESOURCES, AchievementUpdate.UpdateType.UNLOCK, 0)));
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testBuiltScathisOneAndDied() throws Exception {
    instance.builtScathis(1, false, achievementUpdates);
    verifyZeroInteractions(achievementService);
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testBuiltScathisOneAndSurvived() throws Exception {
    instance.builtScathis(1, true, achievementUpdates);
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_MAKE_IT_HAIL, AchievementUpdate.UpdateType.UNLOCK, 0)));
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testBuiltMavorsOneAndDied() throws Exception {
    instance.builtMavors(1, false, achievementUpdates);
    verifyZeroInteractions(achievementService);
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testBuiltMavorsOneAndSurvived() throws Exception {
    instance.builtMavors(1, true, achievementUpdates);
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_I_HAVE_A_CANON, AchievementUpdate.UpdateType.UNLOCK, 0)));
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testLowestAcuHealthZeroDied() throws Exception {
    instance.lowestAcuHealth(0, false, achievementUpdates);
    verifyZeroInteractions(achievementService);
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testLowestAcuHealth499Survived() throws Exception {
    instance.lowestAcuHealth(499, true, achievementUpdates);
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_THAT_WAS_CLOSE, AchievementUpdate.UpdateType.UNLOCK, 0)));
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testLowestAcuHealth500Survived() throws Exception {
    instance.lowestAcuHealth(500, true, achievementUpdates);
    verifyZeroInteractions(achievementService);
    verifyZeroInteractions(eventService);
  }

  @Test
  public void testTopScore7Players() throws Exception {
    instance.highscore(true, 7, achievementUpdates);
    verifyZeroInteractions(achievementService);
  }

  @Test
  public void testTopScore8Players() throws Exception {
    instance.highscore(true, 8, achievementUpdates);
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_TOP_SCORE, AchievementUpdate.UpdateType.UNLOCK, 0)));
    assertThat(achievementUpdates, hasItem(new AchievementUpdate(AchievementId.ACH_UNBEATABLE, AchievementUpdate.UpdateType.INCREMENT, 1)));
  }

  @Test
  public void testProcessAbortProcessingIfNoArmyResult() throws Exception {
    List<ArmyStatistics> stats = readStats("/stats/game_stats_full_example.json");

    game.getReportedArmyOutcomes().clear();

    instance.process(player, game, stats);
    verifyZeroInteractions(achievementService);
    verifyZeroInteractions(eventService);
  }

  private List<ArmyStatistics> readStats(String file) throws java.io.IOException {
    JsonNode node = objectMapper.readTree(getClass().getResourceAsStream(file));
    JsonNode stats = node.get("stats");
    TypeReference<List<ArmyStatistics>> typeReference = new TypeReference<List<ArmyStatistics>>() {
    };

    JsonParser jsonParser = stats.traverse();
    jsonParser.setCodec(objectMapper);
    return jsonParser.readValueAs(typeReference);
  }
}
