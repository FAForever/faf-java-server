package com.faforever.server.stats;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.ArmyOutcome;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.Player;
import com.faforever.server.game.Faction;
import com.faforever.server.game.Outcome;
import com.faforever.server.game.Unit;
import com.faforever.server.statistics.ArmyStatistics;
import com.faforever.server.stats.achievements.AchievementId;
import com.faforever.server.stats.achievements.AchievementService;
import com.faforever.server.stats.achievements.AchievementUpdate;
import com.faforever.server.stats.event.EventId;
import com.faforever.server.stats.event.EventService;
import com.faforever.server.stats.event.EventUpdate;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.ToIntFunction;

import static com.faforever.server.statistics.ArmyStatistics.BrainType.AI;
import static com.faforever.server.statistics.ArmyStatistics.BrainType.HUMAN;
import static com.google.common.base.MoreObjects.firstNonNull;

@Slf4j
@Service
public class ArmyStatisticsService {
  private static final String CIVILIAN_ARMY_NAME = "civilian";

  private final byte ladder1v1FeaturedModId;
  private final AchievementService achievementService;
  private final EventService eventService;
  private final ClientService clientService;

  public ArmyStatisticsService(AchievementService achievementService, EventService eventService, ClientService clientService) {
    this.achievementService = achievementService;
    this.eventService = eventService;
    this.clientService = clientService;
    // FIXME load from database/service
    ladder1v1FeaturedModId = 1;
  }

  public void process(Player player, Game game, List<ArmyStatistics> statistics) {
    int numberOfHumans = 0;
    float highestScore = 0;
    String highestScorerName = null;

    ArmyStatistics armyStats = null;
    int currentIndex = 0;
    int playerArmyId = -1;
    for (ArmyStatistics statsItem : statistics) {
      currentIndex++;
      if (statsItem.getType() == AI && !CIVILIAN_ARMY_NAME.equals(statsItem.getName())) {
        log.debug("Ignoring AI game reported by '{}'", player);
        return;
      }

      if (statsItem.getType() == HUMAN) {
        numberOfHumans += 1;
      }

      if (highestScore < statsItem.getGeneral().getScore()) {
        highestScore = statsItem.getGeneral().getScore();
        highestScorerName = statsItem.getName();
      }

      if (Objects.equals(statsItem.getName(), player.getLogin())) {
        armyStats = statsItem;
        playerArmyId = currentIndex;
      }
    }

    if (playerArmyId == -1) {
      log.warn("No army stats available for player '{}' in game '{}'", player, game);
      return;
    }

    if (numberOfHumans < 2) {
      log.debug("Ignoring single player game reported by '{}'", player);
      return;
    }

    int finalArmyId = playerArmyId;
    Optional<ArmyOutcome> armyOutcome = game.getReportedArmyOutcomes().values().stream()
      .flatMap(Collection::stream)
      .filter(item ->item.getArmyId() == finalArmyId)
      .findFirst();
    if (!armyOutcome.isPresent()) {
      log.warn("No outcome for army '{}´ (player '{}') has been reported for game '{}'. " +
        "Aborting stats processing", playerArmyId, player, game);
      return;
    }

    Outcome outcome = armyOutcome.get().getOutcome();

    log.debug("Processing stats for army '{}' (player '{}') in game '{}'", playerArmyId, player, game);

    Faction faction = armyStats.getFaction();
    ArrayList<AchievementUpdate> achievementUpdates = new ArrayList<>();
    ArrayList<EventUpdate> eventUpdates = new ArrayList<>();
    boolean survived = outcome == Outcome.VICTORY;

    Map<String, ArmyStatistics.UnitStats> unitStats = firstNonNull(armyStats.getUnitStats(), Collections.emptyMap());
    ArmyStatistics.CategoryStats categoryStats = armyStats.getCategoryStats();
    boolean scoredHighest = Objects.equals(highestScorerName, player.getLogin());

    if (survived && game.getGameMod() == ladder1v1FeaturedModId) {
      unlock(AchievementId.ACH_FIRST_SUCCESS, achievementUpdates);
    }

    increment(AchievementId.ACH_NOVICE, 1, achievementUpdates);
    increment(AchievementId.ACH_JUNIOR, 1, achievementUpdates);
    increment(AchievementId.ACH_SENIOR, 1, achievementUpdates);
    increment(AchievementId.ACH_VETERAN, 1, achievementUpdates);
    increment(AchievementId.ACH_ADDICT, 1, achievementUpdates);

    factionPlayed(faction, survived, achievementUpdates, eventUpdates);
    categoryStats(categoryStats, survived, achievementUpdates, eventUpdates);
    killedAcus(categoryStats, survived, achievementUpdates);
    builtMercies(countBuiltUnits(unitStats, Unit.MERCY), achievementUpdates);
    builtFireBeetles(countBuiltUnits(unitStats, Unit.FIRE_BEETLE), achievementUpdates);
    builtSalvations(countBuiltUnits(unitStats, Unit.SALVATION), survived, achievementUpdates);
    builtYolonaOss(countBuiltUnits(unitStats, Unit.YOLONA_OSS), survived, achievementUpdates);
    builtParagons(countBuiltUnits(unitStats, Unit.PARAGON), survived, achievementUpdates);
    builtAtlantis(countBuiltUnits(unitStats, Unit.ATLANTIS), achievementUpdates);
    builtTempests(countBuiltUnits(unitStats, Unit.TEMPEST), achievementUpdates);
    builtScathis(countBuiltUnits(unitStats, Unit.SCATHIS), survived, achievementUpdates);
    builtMavors(countBuiltUnits(unitStats, Unit.MAVOR), survived, achievementUpdates);
    builtCzars(countBuiltUnits(unitStats, Unit.CZAR), achievementUpdates);
    builtAhwassas(countBuiltUnits(unitStats, Unit.AHWASSA), achievementUpdates);
    builtYthothas(countBuiltUnits(unitStats, Unit.YTHOTHA), achievementUpdates);
    builtFatboys(countBuiltUnits(unitStats, Unit.FATBOY), achievementUpdates);
    builtMonkeylords(countBuiltUnits(unitStats, Unit.MONKEYLORD), achievementUpdates);
    builtGalacticColossus(countBuiltUnits(unitStats, Unit.GALACTIC_COLOSSUS), achievementUpdates);
    builtSoulRippers(countBuiltUnits(unitStats, Unit.SOUL_RIPPER), achievementUpdates);
    builtMegaliths(countBuiltUnits(unitStats, Unit.MEGALITH), achievementUpdates);
    builtAsfs(countBuiltUnits(unitStats, Unit.ASFS), achievementUpdates);
    builtTransports(categoryStats.getTransportation().getBuilt(), achievementUpdates);
    builtSacus(categoryStats.getSacu().getBuilt(), achievementUpdates);
    lowestAcuHealth(count(unitStats, ArmyStatistics.UnitStats::getLowestHealth, Unit.ACUS), survived, achievementUpdates);
    highscore(scoredHighest, numberOfHumans, achievementUpdates);

    eventService.executeBatchUpdate(player, eventUpdates);
    achievementService.executeBatchUpdate(player, achievementUpdates)
      .thenAccept((playerAchievements) -> clientService.reportUpdatedAchievements(playerAchievements, player));
  }

  @VisibleForTesting
  void categoryStats(ArmyStatistics.CategoryStats categoryStats, boolean survived, List<AchievementUpdate> achievementUpdates, List<EventUpdate> eventUpdates) {
    int builtAir = categoryStats.getAir().getBuilt();
    int builtLand = categoryStats.getLand().getBuilt();
    int builtNaval = categoryStats.getNaval().getBuilt();
    int builtExperimentals = categoryStats.getExperimental().getBuilt();

    recordEvent(EventId.EVENT_BUILT_AIR_UNITS, builtAir, eventUpdates);
    recordEvent(EventId.EVENT_LOST_AIR_UNITS, categoryStats.getAir().getLost(), eventUpdates);
    recordEvent(EventId.EVENT_BUILT_LAND_UNITS, builtLand, eventUpdates);
    recordEvent(EventId.EVENT_LOST_LAND_UNITS, categoryStats.getLand().getLost(), eventUpdates);
    recordEvent(EventId.EVENT_BUILT_NAVAL_UNITS, builtNaval, eventUpdates);
    recordEvent(EventId.EVENT_LOST_NAVAL_UNITS, categoryStats.getNaval().getLost(), eventUpdates);
    recordEvent(EventId.EVENT_LOST_ACUS, categoryStats.getCdr().getLost(), eventUpdates);
    recordEvent(EventId.EVENT_BUILT_TECH_1_UNITS, categoryStats.getTech1().getBuilt(), eventUpdates);
    recordEvent(EventId.EVENT_LOST_TECH_1_UNITS, categoryStats.getTech1().getLost(), eventUpdates);
    recordEvent(EventId.EVENT_BUILT_TECH_2_UNITS, categoryStats.getTech2().getBuilt(), eventUpdates);
    recordEvent(EventId.EVENT_LOST_TECH_2_UNITS, categoryStats.getTech2().getLost(), eventUpdates);
    recordEvent(EventId.EVENT_BUILT_TECH_3_UNITS, categoryStats.getTech3().getBuilt(), eventUpdates);
    recordEvent(EventId.EVENT_LOST_TECH_3_UNITS, categoryStats.getTech3().getLost(), eventUpdates);
    recordEvent(EventId.EVENT_BUILT_EXPERIMENTALS, builtExperimentals, eventUpdates);
    recordEvent(EventId.EVENT_LOST_EXPERIMENTALS, categoryStats.getExperimental().getLost(), eventUpdates);
    recordEvent(EventId.EVENT_BUILT_ENGINEERS, categoryStats.getEngineer().getBuilt(), eventUpdates);
    recordEvent(EventId.EVENT_LOST_ENGINEERS, categoryStats.getEngineer().getLost(), eventUpdates);

    if (survived) {
      if (builtAir > builtLand && builtAir > builtNaval) {
        increment(AchievementId.ACH_WINGMAN, 1, achievementUpdates);
        increment(AchievementId.ACH_WRIGHT_BROTHER, 1, achievementUpdates);
        increment(AchievementId.ACH_KING_OF_THE_SKIES, 1, achievementUpdates);
      } else if (builtLand > builtAir && builtLand > builtNaval) {
        increment(AchievementId.ACH_MILITIAMAN, 1, achievementUpdates);
        increment(AchievementId.ACH_GRENADIER, 1, achievementUpdates);
        increment(AchievementId.ACH_FIELD_MARSHAL, 1, achievementUpdates);
      } else if (builtNaval > builtLand && builtNaval > builtAir) {
        increment(AchievementId.ACH_LANDLUBBER, 1, achievementUpdates);
        increment(AchievementId.ACH_SEAMAN, 1, achievementUpdates);
        increment(AchievementId.ACH_ADMIRAL_OF_THE_FLEET, 1, achievementUpdates);
      }
    }

    if (builtExperimentals > 0) {
      increment(AchievementId.ACH_DR_EVIL, builtExperimentals, achievementUpdates);
    }

    if (builtExperimentals >= 3) {
      increment(AchievementId.ACH_TECHIE, 1, achievementUpdates);
      increment(AchievementId.ACH_I_LOVE_BIG_TOYS, 1, achievementUpdates);
      increment(AchievementId.ACH_EXPERIMENTALIST, 1, achievementUpdates);
    }
  }

  @VisibleForTesting
  void factionPlayed(Faction faction, boolean survived, List<AchievementUpdate> achievementUpdates, List<EventUpdate> eventUpdates) {
    if (faction == Faction.AEON) {
      recordEvent(EventId.EVENT_AEON_PLAYS, 1, eventUpdates);

      if (survived) {
        recordEvent(EventId.EVENT_AEON_WINS, 1, eventUpdates);
        increment(AchievementId.ACH_AURORA, 1, achievementUpdates);
        increment(AchievementId.ACH_BLAZE, 1, achievementUpdates);
        increment(AchievementId.ACH_SERENITY, 1, achievementUpdates);
      }
    } else if (faction == Faction.CYBRAN) {
      recordEvent(EventId.EVENT_CYBRAN_PLAYS, 1, eventUpdates);

      if (survived) {
        recordEvent(EventId.EVENT_CYBRAN_WINS, 1, eventUpdates);
        increment(AchievementId.ACH_MANTIS, 1, achievementUpdates);
        increment(AchievementId.ACH_WAGNER, 1, achievementUpdates);
        increment(AchievementId.ACH_TREBUCHET, 1, achievementUpdates);
      }
    } else if (faction == Faction.UEF) {
      recordEvent(EventId.EVENT_UEF_PLAYS, 1, eventUpdates);

      if (survived) {
        recordEvent(EventId.EVENT_UEF_WINS, 1, eventUpdates);
        increment(AchievementId.ACH_MA12_STRIKER, 1, achievementUpdates);
        increment(AchievementId.ACH_RIPTIDE, 1, achievementUpdates);
        increment(AchievementId.ACH_DEMOLISHER, 1, achievementUpdates);
      }
    } else if (faction == Faction.SERAPHIM) {
      recordEvent(EventId.EVENT_SERAPHIM_PLAYS, 1, eventUpdates);

      if (survived) {
        recordEvent(EventId.EVENT_SERAPHIM_WINS, 1, eventUpdates);
        increment(AchievementId.ACH_THAAM, 1, achievementUpdates);
        increment(AchievementId.ACH_YENZYNE, 1, achievementUpdates);
        increment(AchievementId.ACH_SUTHANUS, 1, achievementUpdates);
      }
    }
  }

  @VisibleForTesting
  void killedAcus(ArmyStatistics.CategoryStats categoryStats, boolean survived, List<AchievementUpdate> achievementUpdates) {
    int acusPerPlayer = categoryStats.getCdr().getBuilt() > 0 ? categoryStats.getCdr().getBuilt() : 1;
    int killedAcus = categoryStats.getCdr().getKilled();

    // I'm aware that this is not perfectly correct, but it's edge case && tracking who got killed by whom would
    // require game code changes
    if (killedAcus >= 3 * acusPerPlayer && survived) {
      unlock(AchievementId.ACH_HATTRICK, achievementUpdates);
    }

    increment(AchievementId.ACH_DONT_MESS_WITH_ME, killedAcus / acusPerPlayer, achievementUpdates);
  }

  private void builtMercies(int count, List<AchievementUpdate> achievementUpdates) {
    increment(AchievementId.ACH_NO_MERCY, count, achievementUpdates);
  }

  private void builtFireBeetles(int count, List<AchievementUpdate> achievementUpdates) {
    increment(AchievementId.ACH_DEADLY_BUGS, count, achievementUpdates);
  }

  @VisibleForTesting
  void builtSalvations(int count, boolean survived, List<AchievementUpdate> achievementUpdates) {
    if (survived && count > 0) {
      unlock(AchievementId.ACH_RAINMAKER, achievementUpdates);
    }
  }

  @VisibleForTesting
  void builtYolonaOss(int count, boolean survived, List<AchievementUpdate> achievementUpdates) {
    if (survived && count > 0) {
      unlock(AchievementId.ACH_NUCLEAR_WAR, achievementUpdates);
    }
  }

  @VisibleForTesting
  void builtParagons(int count, boolean survived, List<AchievementUpdate> achievementUpdates) {
    if (survived && count > 0) {
      unlock(AchievementId.ACH_SO_MUCH_RESOURCES, achievementUpdates);
    }
  }

  private void builtAtlantis(int count, List<AchievementUpdate> achievementUpdates) {
    increment(AchievementId.ACH_IT_AINT_A_CITY, count, achievementUpdates);
  }

  private void builtTempests(int count, List<AchievementUpdate> achievementUpdates) {
    increment(AchievementId.ACH_STORMY_SEA, count, achievementUpdates);
  }

  @VisibleForTesting
  void builtScathis(int count, boolean survived, List<AchievementUpdate> achievementUpdates) {
    if (survived && count > 0) {
      unlock(AchievementId.ACH_MAKE_IT_HAIL, achievementUpdates);
    }
  }

  @VisibleForTesting
  void builtMavors(int count, boolean survived, List<AchievementUpdate> achievementUpdates) {
    if (survived && count > 0) {
      unlock(AchievementId.ACH_I_HAVE_A_CANON, achievementUpdates);
    }
  }

  private void builtCzars(int count, List<AchievementUpdate> achievementUpdates) {
    increment(AchievementId.ACH_DEATH_FROM_ABOVE, count, achievementUpdates);
  }

  private void builtAhwassas(int count, List<AchievementUpdate> achievementUpdates) {
    increment(AchievementId.ACH_ASS_WASHER, count, achievementUpdates);
  }

  private void builtYthothas(int count, List<AchievementUpdate> achievementUpdates) {
    increment(AchievementId.ACH_ALIEN_INVASION, count, achievementUpdates);
  }

  private void builtFatboys(int count, List<AchievementUpdate> achievementUpdates) {
    increment(AchievementId.ACH_FATTER_IS_BETTER, count, achievementUpdates);
  }

  private void builtMonkeylords(int count, List<AchievementUpdate> achievementUpdates) {
    increment(AchievementId.ACH_ARACHNOLOGIST, count, achievementUpdates);
  }

  private void builtGalacticColossus(int count, List<AchievementUpdate> achievementUpdates) {
    increment(AchievementId.ACH_INCOMING_ROBOTS, count, achievementUpdates);
  }

  private void builtSoulRippers(int count, List<AchievementUpdate> achievementUpdates) {
    increment(AchievementId.ACH_FLYING_DEATH, count, achievementUpdates);
  }

  private void builtMegaliths(int count, List<AchievementUpdate> achievementUpdates) {
    increment(AchievementId.ACH_HOLY_CRAB, count, achievementUpdates);
  }

  private void builtTransports(int count, List<AchievementUpdate> achievementUpdates) {
    increment(AchievementId.ACH_THE_TRANSPORTER, count, achievementUpdates);
  }

  private void builtSacus(int count, List<AchievementUpdate> achievementUpdates) {
    setStepsAtLeast(AchievementId.ACH_WHO_NEEDS_SUPPORT, count, achievementUpdates);
  }

  private void builtAsfs(int count, List<AchievementUpdate> achievementUpdates) {
    setStepsAtLeast(AchievementId.ACH_WHAT_A_SWARM, count, achievementUpdates);
  }

  @VisibleForTesting
  void lowestAcuHealth(int health, boolean survived, List<AchievementUpdate> achievementUpdates) {
    if (0 < health && health < 500 && survived) {
      unlock(AchievementId.ACH_THAT_WAS_CLOSE, achievementUpdates);
    }
  }

  @VisibleForTesting
  void highscore(boolean scoredHighest, int numberOfHumans, List<AchievementUpdate> achievementUpdates) {
    if (scoredHighest && numberOfHumans >= 8) {
      unlock(AchievementId.ACH_TOP_SCORE, achievementUpdates);
      increment(AchievementId.ACH_UNBEATABLE, 1, achievementUpdates);
    }
  }

  private void unlock(AchievementId achievementId, List<AchievementUpdate> achievementUpdates) {
    achievementUpdates.add(new AchievementUpdate(achievementId, AchievementUpdate.UpdateType.UNLOCK, 0));
  }

  private void increment(AchievementId achievementId, int steps, List<AchievementUpdate> achievementUpdates) {
    achievementUpdates.add(new AchievementUpdate(achievementId, AchievementUpdate.UpdateType.INCREMENT, steps));
  }

  private void setStepsAtLeast(AchievementId achievementId, int steps, List<AchievementUpdate> achievementUpdates) {
    achievementUpdates.add(new AchievementUpdate(achievementId, AchievementUpdate.UpdateType.SET_STEPS_AT_LEAST, steps));
  }

  private void recordEvent(EventId eventId, int count, List<EventUpdate> eventUpdates) {
    eventUpdates.add(new EventUpdate(eventId, count));
  }

  private int countBuiltUnits(Map<String, ArmyStatistics.UnitStats> unitStats, Unit... units) {
    return count(unitStats, ArmyStatistics.UnitStats::getBuilt, units);
  }

  private int count(Map<String, ArmyStatistics.UnitStats> unitStats, ToIntFunction<ArmyStatistics.UnitStats> function, Unit... units) {
    return unitStats.keySet().stream()
      .filter(unitId -> Arrays.stream(units).anyMatch(u -> u.getUnitId().equals(unitId)))
      .map(unitStats::get)
      .mapToInt(function)
      .sum();
  }
}

