package com.faforever.server.coop;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CoopLeaderboardRepository extends JpaRepository<CoopLeaderboardEntry, Integer> {
}
