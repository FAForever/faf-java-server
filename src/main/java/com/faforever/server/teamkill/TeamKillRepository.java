package com.faforever.server.teamkill;

import com.faforever.server.entity.TeamKill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamKillRepository extends JpaRepository<TeamKill, Integer> {
}
