package com.faforever.server.map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Ladder1v1MapRepository extends JpaRepository<MapVersion, Integer> {
  @Query("SELECT game.mapVersion FROM Game game JOIN game.playerStats playerStats WHERE playerStats.player.id = :playerId AND game.featuredMod.id = :modId ORDER BY game.startTime DESC")
  Page<MapVersion> findRecentlyPlayedLadderMapVersions(Pageable pageable, @Param("playerId") int playerId, @Param("modId") int ladderModId);
}
