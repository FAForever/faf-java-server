package com.faforever.server.stats;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.ArmyOutcome;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.Player;
import com.faforever.server.game.Faction;
import com.faforever.server.game.Outcome;
import com.faforever.server.game.Unit;
import com.faforever.server.mod.ModService;
import com.faforever.server.stats.ArmyStatistics.CategoryStats;
import com.faforever.server.stats.achievements.AchievementId;
import com.faforever.server.stats.achievements.AchievementService;
import com.faforever.server.stats.achievements.AchievementUpdate;
import com.faforever.server.stats.event.EventId;
import com.faforever.server.stats.event.EventService;
import com.faforever.server.stats.event.EventUpdate;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ToIntFunction;

import static com.faforever.server.stats.ArmyStatistics.BrainType.AI;
import static com.faforever.server.stats.ArmyStatistics.BrainType.HUMAN;
import static com.google.common.base.MoreObjects.firstNonNull;

@Slf4j
@Service
public class ArmyStatisticsService {
  private static final String CIVILIAN_ARMY_NAME = "civilian";

  private final AchievementService achievementService;
  private final EventService eventService;
  private final ClientService clientService;
  private final ModService modService;

  public ArmyStatisticsService(AchievementService achievementService, EventService eventService, ClientService clientService, ModService modService) {
    this.achievementService = achievementService;
    this.eventService = eventService;
    this.clientService = clientService;
    this.modService = modService;
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
        log.debug("AI game reported by '{}', aborting stats processing", player);
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
      log.warn("No army stats available for player '{}' in game '{}', aborting stats processing", player, game);
      return;
    }

    if (numberOfHumans < 2) {
      log.debug("Single player game '{}' reported by '{}', aborting stats processing", game, player);
      return;
    }

    int finalArmyId = playerArmyId;
    Optional<ArmyOutcome> armyOutcome = game.getReportedArmyOutcomes().values().stream()
      .flatMap(Collection::stream)
      .filter(item -> item.getArmyId() == finalArmyId)
      .findFirst();
    if (!armyOutcome.isPresent()) {
      log.warn("No outcome for army '{}´ (player '{}') has been reported for game '{}', " +
        "aborting stats processing", playerArmyId, player, game);
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

    int playerId = player.getId();
    if (survived && modService.isLadder1v1(game.getFeaturedMod())) {
      unlock(AchievementId.ACH_FIRST_SUCCESS, achievementUpdates, playerId);
    }

    increment(AchievementId.ACH_NOVICE, 1, achievementUpdates, playerId);
    increment(AchievementId.ACH_JUNIOR, 1, achievementUpdates, playerId);
    increment(AchievementId.ACH_SENIOR, 1, achievementUpdates, playerId);
    increment(AchievementId.ACH_VETERAN, 1, achievementUpdates, playerId);
    increment(AchievementId.ACH_ADDICT, 1, achievementUpdates, playerId);

    factionPlayed(faction, survived, achievementUpdates, eventUpdates, playerId);
    categoryStats(categoryStats, survived, achievementUpdates, eventUpdates, playerId);
    killedAcus(categoryStats, survived, achievementUpdates, playerId);
    builtMercies(countBuiltUnits(unitStats, Unit.MERCY), achievementUpdates, playerId);
    builtFireBeetles(countBuiltUnits(unitStats, Unit.FIRE_BEETLE), achievementUpdates, playerId);
    builtSalvations(countBuiltUnits(unitStats, Unit.SALVATION), survived, achievementUpdates, playerId);
    builtYolonaOss(countBuiltUnits(unitStats, Unit.YOLONA_OSS), survived, achievementUpdates, playerId);
    builtParagons(countBuiltUnits(unitStats, Unit.PARAGON), survived, achievementUpdates, playerId);
    builtAtlantis(countBuiltUnits(unitStats, Unit.ATLANTIS), achievementUpdates, playerId);
    builtTempests(countBuiltUnits(unitStats, Unit.TEMPEST), achievementUpdates, playerId);
    builtScathis(countBuiltUnits(unitStats, Unit.SCATHIS), survived, achievementUpdates, playerId);
    builtMavors(countBuiltUnits(unitStats, Unit.MAVOR), survived, achievementUpdates, playerId);
    builtCzars(countBuiltUnits(unitStats, Unit.CZAR), achievementUpdates, playerId);
    builtAhwassas(countBuiltUnits(unitStats, Unit.AHWASSA), achievementUpdates, playerId);
    builtYthothas(countBuiltUnits(unitStats, Unit.YTHOTHA), achievementUpdates, playerId);
    builtFatboys(countBuiltUnits(unitStats, Unit.FATBOY), achievementUpdates, playerId);
    builtMonkeylords(countBuiltUnits(unitStats, Unit.MONKEYLORD), achievementUpdates, playerId);
    builtGalacticColossus(countBuiltUnits(unitStats, Unit.GALACTIC_COLOSSUS), achievementUpdates, playerId);
    builtSoulRippers(countBuiltUnits(unitStats, Unit.SOUL_RIPPER), achievementUpdates, playerId);
    builtMegaliths(countBuiltUnits(unitStats, Unit.MEGALITH), achievementUpdates, playerId);
    builtAsfs(countBuiltUnits(unitStats, Unit.ASFS), achievementUpdates, playerId);
    builtTransports(categoryStats.getTransportation().getBuilt(), achievementUpdates, playerId);
    builtSacus(categoryStats.getSacu().getBuilt(), achievementUpdates, playerId);
    lowestAcuHealth(count(unitStats, ArmyStatistics.UnitStats::getLowestHealth, Unit.ACUS), survived, achievementUpdates, playerId);
    highscore(scoredHighest, numberOfHumans, achievementUpdates, playerId);

    eventService.executeBatchUpdate(eventUpdates)
      .exceptionally(throwable -> {
        log.warn("Could not report '" + eventUpdates.size() + "' event updates for player '" + player + "'", throwable);
        return null;
      });

    achievementService.executeBatchUpdate(achievementUpdates)
      .thenAccept(playerAchievements -> clientService.reportUpdatedAchievements(playerAchievements, player))
      .exceptionally(throwable -> {
        log.warn("Could not report '" + achievementUpdates.size() + "' achievement updates for player '" + player + "'", throwable);
        return null;
      });
  }

  private void builtMercies(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    increment(AchievementId.ACH_NO_MERCY, count, achievementUpdates, playerId);
  }

  private void builtFireBeetles(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    increment(AchievementId.ACH_DEADLY_BUGS, count, achievementUpdates, playerId);
  }

  private void builtAtlantis(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    increment(AchievementId.ACH_IT_AINT_A_CITY, count, achievementUpdates, playerId);
  }

  private void builtTempests(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    increment(AchievementId.ACH_STORMY_SEA, count, achievementUpdates, playerId);
  }

  private void builtCzars(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    increment(AchievementId.ACH_DEATH_FROM_ABOVE, count, achievementUpdates, playerId);
  }

  private void builtAhwassas(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    increment(AchievementId.ACH_ASS_WASHER, count, achievementUpdates, playerId);
  }

  private void builtYthothas(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    increment(AchievementId.ACH_ALIEN_INVASION, count, achievementUpdates, playerId);
  }

  private void builtFatboys(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    increment(AchievementId.ACH_FATTER_IS_BETTER, count, achievementUpdates, playerId);
  }

  private void builtMonkeylords(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    increment(AchievementId.ACH_ARACHNOLOGIST, count, achievementUpdates, playerId);
  }

  private void builtGalacticColossus(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    increment(AchievementId.ACH_INCOMING_ROBOTS, count, achievementUpdates, playerId);
  }

  private void builtSoulRippers(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    increment(AchievementId.ACH_FLYING_DEATH, count, achievementUpdates, playerId);
  }

  private void builtMegaliths(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    increment(AchievementId.ACH_HOLY_CRAB, count, achievementUpdates, playerId);
  }

  private void builtTransports(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    increment(AchievementId.ACH_THE_TRANSPORTER, count, achievementUpdates, playerId);
  }

  private void builtSacus(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    setStepsAtLeast(AchievementId.ACH_WHO_NEEDS_SUPPORT, count, achievementUpdates, playerId);
  }

  private void builtAsfs(int count, List<AchievementUpdate> achievementUpdates, int playerId) {
    setStepsAtLeast(AchievementId.ACH_WHAT_A_SWARM, count, achievementUpdates, playerId);
  }

  private void unlock(AchievementId achievementId, List<AchievementUpdate> achievementUpdates, int playerId) {
    achievementUpdates.add(new AchievementUpdate(playerId, achievementId, AchievementUpdate.UpdateType.UNLOCK, 0));
  }

  private void increment(AchievementId achievementId, int steps, List<AchievementUpdate> achievementUpdates, int playerId) {
    achievementUpdates.add(new AchievementUpdate(playerId, achievementId, AchievementUpdate.UpdateType.INCREMENT, steps));
  }

  private void setStepsAtLeast(AchievementId achievementId, int steps, List<AchievementUpdate> achievementUpdates, int playerId) {
    achievementUpdates.add(new AchievementUpdate(playerId, achievementId, AchievementUpdate.UpdateType.SET_STEPS_AT_LEAST, steps));
  }

  private void recordEvent(EventId eventId, int count, List<EventUpdate> eventUpdates, int playerId) {
    eventUpdates.add(new EventUpdate(playerId, eventId, count));
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

  @VisibleForTesting
  void categoryStats(CategoryStats categoryStats, boolean survived, List<AchievementUpdate> achievementUpdates, List<EventUpdate> eventUpdates, int playerId) {
    int builtAir = categoryStats.getAir().getBuilt();
    int builtLand = categoryStats.getLand().getBuilt();
    int builtNaval = categoryStats.getNaval().getBuilt();
    int builtExperimentals = categoryStats.getExperimental().getBuilt();

    recordEvent(EventId.EVENT_BUILT_AIR_UNITS, builtAir, eventUpdates, playerId);
    recordEvent(EventId.EVENT_LOST_AIR_UNITS, categoryStats.getAir().getLost(), eventUpdates, playerId);
    recordEvent(EventId.EVENT_BUILT_LAND_UNITS, builtLand, eventUpdates, playerId);
    recordEvent(EventId.EVENT_LOST_LAND_UNITS, categoryStats.getLand().getLost(), eventUpdates, playerId);
    recordEvent(EventId.EVENT_BUILT_NAVAL_UNITS, builtNaval, eventUpdates, playerId);
    recordEvent(EventId.EVENT_LOST_NAVAL_UNITS, categoryStats.getNaval().getLost(), eventUpdates, playerId);
    recordEvent(EventId.EVENT_LOST_ACUS, categoryStats.getCdr().getLost(), eventUpdates, playerId);
    recordEvent(EventId.EVENT_BUILT_TECH_1_UNITS, categoryStats.getTech1().getBuilt(), eventUpdates, playerId);
    recordEvent(EventId.EVENT_LOST_TECH_1_UNITS, categoryStats.getTech1().getLost(), eventUpdates, playerId);
    recordEvent(EventId.EVENT_BUILT_TECH_2_UNITS, categoryStats.getTech2().getBuilt(), eventUpdates, playerId);
    recordEvent(EventId.EVENT_LOST_TECH_2_UNITS, categoryStats.getTech2().getLost(), eventUpdates, playerId);
    recordEvent(EventId.EVENT_BUILT_TECH_3_UNITS, categoryStats.getTech3().getBuilt(), eventUpdates, playerId);
    recordEvent(EventId.EVENT_LOST_TECH_3_UNITS, categoryStats.getTech3().getLost(), eventUpdates, playerId);
    recordEvent(EventId.EVENT_BUILT_EXPERIMENTALS, builtExperimentals, eventUpdates, playerId);
    recordEvent(EventId.EVENT_LOST_EXPERIMENTALS, categoryStats.getExperimental().getLost(), eventUpdates, playerId);
    recordEvent(EventId.EVENT_BUILT_ENGINEERS, categoryStats.getEngineer().getBuilt(), eventUpdates, playerId);
    recordEvent(EventId.EVENT_LOST_ENGINEERS, categoryStats.getEngineer().getLost(), eventUpdates, playerId);

    if (survived) {
      if (builtAir > builtLand && builtAir > builtNaval) {
        increment(AchievementId.ACH_WINGMAN, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_WRIGHT_BROTHER, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_KING_OF_THE_SKIES, 1, achievementUpdates, playerId);
      } else if (builtLand > builtAir && builtLand > builtNaval) {
        increment(AchievementId.ACH_MILITIAMAN, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_GRENADIER, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_FIELD_MARSHAL, 1, achievementUpdates, playerId);
      } else if (builtNaval > builtLand && builtNaval > builtAir) {
        increment(AchievementId.ACH_LANDLUBBER, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_SEAMAN, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_ADMIRAL_OF_THE_FLEET, 1, achievementUpdates, playerId);
      }
    }

    if (builtExperimentals > 0) {
      increment(AchievementId.ACH_DR_EVIL, builtExperimentals, achievementUpdates, playerId);
    }

    if (builtExperimentals >= 3) {
      increment(AchievementId.ACH_TECHIE, 1, achievementUpdates, playerId);
      increment(AchievementId.ACH_I_LOVE_BIG_TOYS, 1, achievementUpdates, playerId);
      increment(AchievementId.ACH_EXPERIMENTALIST, 1, achievementUpdates, playerId);
    }
  }

  @VisibleForTesting
  void factionPlayed(Faction faction, boolean survived, List<AchievementUpdate> achievementUpdates, List<EventUpdate> eventUpdates, int playerId) {
    if (faction == Faction.AEON) {
      recordEvent(EventId.EVENT_AEON_PLAYS, 1, eventUpdates, playerId);

      if (survived) {
        recordEvent(EventId.EVENT_AEON_WINS, 1, eventUpdates, playerId);
        increment(AchievementId.ACH_AURORA, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_BLAZE, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_SERENITY, 1, achievementUpdates, playerId);
      }
    } else if (faction == Faction.CYBRAN) {
      recordEvent(EventId.EVENT_CYBRAN_PLAYS, 1, eventUpdates, playerId);

      if (survived) {
        recordEvent(EventId.EVENT_CYBRAN_WINS, 1, eventUpdates, playerId);
        increment(AchievementId.ACH_MANTIS, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_WAGNER, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_TREBUCHET, 1, achievementUpdates, playerId);
      }
    } else if (faction == Faction.UEF) {
      recordEvent(EventId.EVENT_UEF_PLAYS, 1, eventUpdates, playerId);

      if (survived) {
        recordEvent(EventId.EVENT_UEF_WINS, 1, eventUpdates, playerId);
        increment(AchievementId.ACH_MA12_STRIKER, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_RIPTIDE, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_DEMOLISHER, 1, achievementUpdates, playerId);
      }
    } else if (faction == Faction.SERAPHIM) {
      recordEvent(EventId.EVENT_SERAPHIM_PLAYS, 1, eventUpdates, playerId);

      if (survived) {
        recordEvent(EventId.EVENT_SERAPHIM_WINS, 1, eventUpdates, playerId);
        increment(AchievementId.ACH_THAAM, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_YENZYNE, 1, achievementUpdates, playerId);
        increment(AchievementId.ACH_SUTHANUS, 1, achievementUpdates, playerId);
      }
    }
  }

  @VisibleForTesting
  void killedAcus(CategoryStats categoryStats, boolean survived, List<AchievementUpdate> achievementUpdates, int playerId) {
    int acusPerPlayer = categoryStats.getCdr().getBuilt() > 0 ? categoryStats.getCdr().getBuilt() : 1;
    int killedAcus = categoryStats.getCdr().getKilled();

    // I'm aware that this is not perfectly correct, but it's edge case && tracking who got killed by whom would
    // require game code changes
    if (killedAcus >= 3 * acusPerPlayer && survived) {
      unlock(AchievementId.ACH_HATTRICK, achievementUpdates, playerId);
    }

    increment(AchievementId.ACH_DONT_MESS_WITH_ME, killedAcus / acusPerPlayer, achievementUpdates, playerId);
  }

  @VisibleForTesting
  void builtSalvations(int count, boolean survived, List<AchievementUpdate> achievementUpdates, int playerId) {
    if (survived && count > 0) {
      unlock(AchievementId.ACH_RAINMAKER, achievementUpdates, playerId);
    }
  }

  @VisibleForTesting
  void builtYolonaOss(int count, boolean survived, List<AchievementUpdate> achievementUpdates, int playerId) {
    if (survived && count > 0) {
      unlock(AchievementId.ACH_NUCLEAR_WAR, achievementUpdates, playerId);
    }
  }

  @VisibleForTesting
  void builtParagons(int count, boolean survived, List<AchievementUpdate> achievementUpdates, int playerId) {
    if (survived && count > 0) {
      unlock(AchievementId.ACH_SO_MUCH_RESOURCES, achievementUpdates, playerId);
    }
  }

  @VisibleForTesting
  void builtScathis(int count, boolean survived, List<AchievementUpdate> achievementUpdates, int playerId) {
    if (survived && count > 0) {
      unlock(AchievementId.ACH_MAKE_IT_HAIL, achievementUpdates, playerId);
    }
  }

  @VisibleForTesting
  void builtMavors(int count, boolean survived, List<AchievementUpdate> achievementUpdates, int playerId) {
    if (survived && count > 0) {
      unlock(AchievementId.ACH_I_HAVE_A_CANON, achievementUpdates, playerId);
    }
  }

  @VisibleForTesting
  void lowestAcuHealth(int health, boolean survived, List<AchievementUpdate> achievementUpdates, int playerId) {
    if (0 < health && health < 500 && survived) {
      unlock(AchievementId.ACH_THAT_WAS_CLOSE, achievementUpdates, playerId);
    }
  }

  @VisibleForTesting
  void highscore(boolean scoredHighest, int numberOfHumans, List<AchievementUpdate> achievementUpdates, int playerId) {
    if (scoredHighest && numberOfHumans >= 8) {
      unlock(AchievementId.ACH_TOP_SCORE, achievementUpdates, playerId);
      increment(AchievementId.ACH_UNBEATABLE, 1, achievementUpdates, playerId);
    }
  }
}

