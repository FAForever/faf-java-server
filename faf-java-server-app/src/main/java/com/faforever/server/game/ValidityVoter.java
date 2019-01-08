package com.faforever.server.game;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.mod.ModService;
import com.faforever.server.mod.ModVersion;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.faforever.server.game.GameService.NO_TEAM_ID;
import static com.faforever.server.game.GameService.OPTION_CHEATS_ENABLED;
import static com.faforever.server.game.GameService.OPTION_FOG_OF_WAR;
import static com.faforever.server.game.GameService.OPTION_NO_RUSH;
import static com.faforever.server.game.GameService.OPTION_PREBUILT_UNITS;
import static com.faforever.server.game.GameService.OPTION_RESTRICTED_CATEGORIES;
import static com.faforever.server.game.GameService.OPTION_TEAM;
import static com.faforever.server.game.GameService.OPTION_TEAM_LOCK;

@UtilityClass
class ValidityVoter {

  private boolean areTeamsEven(Game game) {
    Map<Integer, Long> playersPerTeam = game.getPlayerStats().values().stream()
      .map(GamePlayerStats::getTeam)
      .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    if (playersPerTeam.containsKey(NO_TEAM_ID)) {
      // There are players without a team, all other teams must have exactly 1 player
      return playersPerTeam.entrySet().stream()
        .filter(teamToCount -> teamToCount.getKey() != NO_TEAM_ID)
        .allMatch(teamToCount -> teamToCount.getValue() == 1);
    }
    // All teams must have the same amount of players
    return playersPerTeam.values().stream().distinct().count() == 1;
  }

  private boolean isFreeForAll(Game game) {
    Map<Integer, Map<String, Object>> playerOptions = game.getPlayerOptions();
    if (playerOptions.size() < 3) {
      return false;
    }
    Set<Integer> teams = new HashSet<>();
    for (Map<String, Object> options : playerOptions.values()) {
      int team = (int) options.getOrDefault(OPTION_TEAM, NO_TEAM_ID);
      if (team != NO_TEAM_ID) {
        if (teams.contains(team)) {
          return false;
        }
        teams.add(team);
      }
    }
    return true;
  }

  Function<Game, Validity> gameLengthVoter(ServerProperties properties) {
    return game -> {
      int minSeconds = game.getPlayerStats().size() * properties.getGame().getRankedMinTimeMultiplicator();
      return Duration.between(Instant.now(), game.getStartTime()).getSeconds() < minSeconds ? Validity.TOO_SHORT : Validity.VALID;
    };
  }

  Function<Game, Validity> gameResultVoter() {
    return game -> game.getReportedArmyResults().isEmpty() || game.getReportedArmyResults().isEmpty() ? Validity.UNKNOWN_RESULT : Validity.VALID;
  }

  Function<Game, Validity> singlePlayerVoter() {
    return game -> game.getPlayerStats().size() < 2 ? Validity.SINGLE_PLAYER : Validity.VALID;
  }

  Function<Game, Validity> mutualDrawVoter() {
    return game -> game.isMutuallyAgreedDraw() ? Validity.MUTUAL_DRAW : Validity.VALID;
  }

  Function<Game, Validity> desyncVoter() {
    return game -> game.getDesyncCounter().intValue() > game.getPlayerStats().size() ? Validity.TOO_MANY_DESYNCS : Validity.VALID;
  }

  Function<Game, Validity> teamsUnlockedVoter() {
    return game -> "unlocked".equals(game.getOptions().get(OPTION_TEAM_LOCK)) ? Validity.TEAMS_UNLOCKED : Validity.VALID;
  }

  Function<Game, Validity> rankedMapVoter() {
    return game -> game.getMapVersion() == null || !game.getMapVersion().isRanked() ? Validity.BAD_MAP : Validity.VALID;
  }

  Function<Game, Validity> restrictedUnitsVoter() {
    return game -> game.getOptions().containsKey(OPTION_RESTRICTED_CATEGORIES) && (int) game.getOptions().get(OPTION_RESTRICTED_CATEGORIES) != 0 ?
      Validity.BAD_UNIT_RESTRICTIONS : Validity.VALID;
  }

  Function<Game, Validity> noRushVoter() {
    return game -> !"Off".equals(game.getOptions().get(OPTION_NO_RUSH)) ? Validity.NO_RUSH_ENABLED : Validity.VALID;
  }

  Function<Game, Validity> prebuiltUnitsVoter() {
    return game -> !"Off".equals(game.getOptions().get(OPTION_PREBUILT_UNITS)) ? Validity.PREBUILT_ENABLED : Validity.VALID;
  }

  Function<Game, Validity> cheatsEnabledVoter() {
    return game -> !"false".equals(game.getOptions().get(OPTION_CHEATS_ENABLED)) ? Validity.CHEATS_ENABLED : Validity.VALID;
  }

  Function<Game, Validity> fogOfWarVoter() {
    return game -> !"explored".equals(game.getOptions().get(OPTION_FOG_OF_WAR)) ? Validity.NO_FOG_OF_WAR : Validity.VALID;
  }

  Function<Game, Validity> evenTeamsVoter(ModService modService) {
    return game -> !areTeamsEven(game) && !modService.isCoop(game.getFeaturedMod()) ? Validity.UNEVEN_TEAMS : Validity.VALID;
  }

  Function<Game, Validity> freeForAllVoter() {
    return game -> isFreeForAll(game) ? Validity.FREE_FOR_ALL : Validity.VALID;
  }

  Function<Game, Validity> victoryConditionVoter(ModService modService) {
    return game -> {
      if (modService.isCoop(game.getFeaturedMod())) {
        return game.getVictoryCondition() != VictoryCondition.SANDBOX
          ? Validity.WRONG_VICTORY_CONDITION : Validity.VALID;
      }
      return game.getVictoryCondition() != VictoryCondition.DEMORALIZATION
        ? Validity.WRONG_VICTORY_CONDITION : Validity.VALID;
    };
  }

  Function<Game, Validity> isRankedVoter(ModService modService) {
    return game -> !game.getSimMods().stream().allMatch(ModVersion::isRanked) ? Validity.BAD_MOD : Validity.VALID;
  }

  Function<Game, Validity> hasAiVoter() {
    return game -> game.getAiOptions().size() > 0 ? Validity.HAS_AI : Validity.VALID;
  }
}
