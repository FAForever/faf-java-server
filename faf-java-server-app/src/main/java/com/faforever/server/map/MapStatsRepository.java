package com.faforever.server.map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapStatsRepository extends JpaRepository<MapStats, Integer> {
}
