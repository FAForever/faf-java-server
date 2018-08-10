package com.faforever.server.ladder1v1;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.player.Player;
import com.faforever.server.player.PlayerDivisionInfo;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class DivisionService {
  private final ServerProperties properties;
  private final DivisionRepository divisionRepository;
  private final PlayerDivisionInfoRepository playerDivisionInfoRepository;

  @Getter
  private ImmutableList<Division> divisions;

  @Inject
  public DivisionService(ServerProperties properties, DivisionRepository divisionRepository, PlayerDivisionInfoRepository playerDivisionInfoRepository) {
    this.properties = properties;
    this.divisionRepository = divisionRepository;
    this.playerDivisionInfoRepository = playerDivisionInfoRepository;
  }

  @PostConstruct
  public void init() {
    divisions = ImmutableList.copyOf(divisionRepository.findAllByOrderByLeagueAscThresholdAsc());
  }

  public Optional<Division> getCurrentPlayerDivision(Player player) {
    int season = properties.getLadder1v1().getSeason();

    PlayerDivisionInfo info = playerDivisionInfoRepository.findByPlayerAndSeason(player, season);

    if (info == null) {
      return Optional.empty();
    }

    return Optional.of(
      divisions.stream()
        .filter(division -> Objects.equals(division.getLeague(), info.getLeague()))
        .filter(division -> info.getScore() <= division.getThreshold())
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not determine division for PlayerDivisionInfo {0}", info)))
    );
  }

  @Transactional
  public void postResult(@NotNull Player playerOne, @NotNull Player playerTwo, @Nullable Player winner) {
    PlayerDivisionInfo playerOneInfo = getPlayerDivisionInfo(playerOne, properties.getLadder1v1().getSeason());
    PlayerDivisionInfo playerTwoInfo = getPlayerDivisionInfo(playerTwo, properties.getLadder1v1().getSeason());

    playerOneInfo.setGames(playerOneInfo.getGames() + 1);
    playerTwoInfo.setGames(playerTwoInfo.getGames() + 1);

    if (winner == null) {
      log.debug("Game ended in a draw, no changes in score");
      return;
    }

    PlayerDivisionInfo winnerInfo;
    PlayerDivisionInfo loserInfo;

    if (winner.equals(playerOne)) {
      winnerInfo = playerOneInfo;
      loserInfo = playerTwoInfo;
    } else if (winner.equals(playerTwo)) {
      winnerInfo = playerTwoInfo;
      loserInfo = playerOneInfo;
    } else {
      throw new IllegalArgumentException(MessageFormat.format("Winner does not match any of the players (player one: '{0}', player two: '{1}', winner: '{2}'", playerOne, playerTwo, winner));
    }

    float gain = properties.getLadder1v1().getRegularGain();
    float loss = properties.getLadder1v1().getRegularLoss();

    if (winnerInfo.isInInferiorLeague(loserInfo)) {
      gain = properties.getLadder1v1().getIncreasedGain();
      loss = properties.getLadder1v1().getIncreasedLoss();
    } else if (winnerInfo.isInSuperiorLeague(loserInfo)) {
      gain = properties.getLadder1v1().getReducedGain();
      loss = properties.getLadder1v1().getReducedLoss();
    }

    log.debug("Player '{}' won against player '{}', gain: '{}', loss: '{}'", winner, loserInfo.getPlayer(), gain, loss);

    if (winnerInfo.getScore() + gain > getMaxLeagueThreshold(winnerInfo.getLeague())) {
      int newLeague = winnerInfo.getLeague() + 1;
      log.debug("Winner was promoted to league '{}'", newLeague);
      winnerInfo.setScore(0f);
      winnerInfo.setLeague(newLeague);
    } else {
      winnerInfo.setScore(winnerInfo.getScore() + gain);
    }

    loserInfo.setScore(Float.max(0f, loserInfo.getScore() - loss));
  }

  private int getMaxLeagueThreshold(int league) {
    return divisions.stream()
      .filter(division -> division.getLeague() == league)
      .mapToInt(Division::getThreshold)
      .max()
      .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not calculate maximum threshold of league: {0}", league)));
  }

  @NotNull
  private PlayerDivisionInfo addPlayerToSeason(@NotNull Player player, int season) {
    log.debug("Adding player '{}' to season '{}'", player, season);

    PlayerDivisionInfo playerDivisionInfo = new PlayerDivisionInfo();
    playerDivisionInfo.setSeason(season);
    playerDivisionInfo.setPlayer(player);
    playerDivisionInfo.setGames(0);
    playerDivisionInfo.setLeague(1);
    playerDivisionInfo.setScore(0f);

    playerDivisionInfoRepository.save(playerDivisionInfo);
    return playerDivisionInfo;
  }

  @NotNull
  private PlayerDivisionInfo getPlayerDivisionInfo(@NotNull Player player, int season) {
    PlayerDivisionInfo playerDivisionInfo = playerDivisionInfoRepository.findByPlayerAndSeason(player, season);

    if (playerDivisionInfo == null) {
      playerDivisionInfo = addPlayerToSeason(player, season);
    }

    return playerDivisionInfo;
  }
}
