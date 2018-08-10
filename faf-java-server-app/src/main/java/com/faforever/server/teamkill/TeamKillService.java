package com.faforever.server.teamkill;

import com.faforever.server.game.Game;
import com.faforever.server.game.TeamKill;
import com.faforever.server.player.Player;
import com.faforever.server.player.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

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
    Optional<Player> victim = playerService.getOnlinePlayer(victimId);
    Optional<Player> killer = playerService.getOnlinePlayer(killerId);

    Game game = player.getCurrentGame();
    if (game == null) {
      log.warn("Player '{}' reported team kill by '{}' but is not associated with a game", player, killer);
      return;
    }

    // Player's shouldn't even be able to specify a victim, but that's what the current protocol allows.
    if (victimId != player.getId()) {
      log.warn("Player '{}' reported team kill by '{}' for player '{}'", player, killer, victim);
      return;
    }

    if (!killer.isPresent()) {
      log.warn("Player '{}' reported team kill by unknown player '{}' in game '{}' (victim: '{}')", player, killerId, game, victim);
      return;
    }

    boolean isKillerPartOfGame = game.getPlayerStats().values().stream()
      .anyMatch(gamePlayerStats -> gamePlayerStats.getPlayer().getId() == killerId);

    if (!isKillerPartOfGame) {
      log.warn("Player '{}' reported team kill by '{}' in game '{}', but killer is not part of the game", player, killer, game);
    }

    log.debug("Player '{}' reported team kill by '{}' in game: {}", victim, killer, game);

    TeamKill teamKill = new TeamKill(0, killerId, victimId, game.getId(), (int) timeDelta.getSeconds(), Timestamp.from(Instant.now()));
    teamKillRepository.save(teamKill);
  }
}
