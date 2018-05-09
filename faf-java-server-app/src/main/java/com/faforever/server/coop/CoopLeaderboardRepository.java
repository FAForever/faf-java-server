package com.faforever.server.coop;

import com.faforever.server.entity.CoopLeaderboardEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoopLeaderboardRepository extends JpaRepository<CoopLeaderboardEntry, Integer> {
}
