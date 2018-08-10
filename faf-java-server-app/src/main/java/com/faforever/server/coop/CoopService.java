package com.faforever.server.coop;

import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.Requests;
import com.faforever.server.game.Game;
import com.faforever.server.player.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Time;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CoopService {
  private final CoopMapRepository coopMapRepository;
  private final CoopLeaderboardRepository coopLeaderboardRepository;

  @Inject
  public CoopService(CoopMapRepository coopMapRepository, CoopLeaderboardRepository coopLeaderboardRepository) {
    this.coopMapRepository = coopMapRepository;
    this.coopLeaderboardRepository = coopLeaderboardRepository;
  }

  public void reportOperationComplete(Player player, boolean secondaryTargets, Duration duration) {
    Game game = player.getCurrentGame();
    Requests.verify(game != null, ErrorCode.COOP_CANT_REPORT_NOT_IN_GAME);

    log.debug("Player '{}' reported coop result '{}' with secondary targets '{}' for game: {}", player, duration, secondaryTargets, game);

    Optional<CoopMap> optional = coopMapRepository.findOneByFilenameLikeIgnoreCase(game.getMapFolderName());
    if (optional.isPresent()) {
      CoopLeaderboardEntry coopLeaderboardEntry = new CoopLeaderboardEntry();
      coopLeaderboardEntry.setGameId(game.getId());
      coopLeaderboardEntry.setMission(optional.get());
      coopLeaderboardEntry.setPlayerCount(game.getPlayerStats().size());
      coopLeaderboardEntry.setSecondary(secondaryTargets);
      coopLeaderboardEntry.setTime(new Time(duration.toMillis()));

      coopLeaderboardRepository.save(coopLeaderboardEntry);
    }
  }

  public List<CoopMap> getMaps() {
    return coopMapRepository.findAll();
  }
}
