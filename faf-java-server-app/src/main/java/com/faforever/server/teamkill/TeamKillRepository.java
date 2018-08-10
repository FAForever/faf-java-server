package com.faforever.server.teamkill;

import com.faforever.server.game.TeamKill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamKillRepository extends JpaRepository<TeamKill, Integer> {
}
