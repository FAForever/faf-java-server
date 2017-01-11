package com.faforever.server.teamkill;

import com.faforever.server.entity.Game;
import com.faforever.server.entity.Player;
import com.faforever.server.player.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class TeamKillService {
  private final PlayerService playerService;
  private final TeamKillRepository teamKillRepository;

  public TeamKillService(PlayerService playerService, TeamKillRepository teamKillRepository) {
    this.playerService = playerService;
    this.teamKillRepository = teamKillRepository;
  }

  public void reportTeamKill(Player player, Duration timeDelta, int killerId, int victimId) {
    Player victim = playerService.getPlayer(victimId);
    Player killer = playerService.getPlayer(killerId);

    Game game = player.getCurrentGame();
    if (game == null) {
      log.warn("Player '{}' reported to have been team killed by '{}' but is not associated with a game", victim, killer);
      return;
    }

    log.warn("Player '{}' reported to have been team killed by '{}' in game: {}", victim, killer, game);
  }
}
