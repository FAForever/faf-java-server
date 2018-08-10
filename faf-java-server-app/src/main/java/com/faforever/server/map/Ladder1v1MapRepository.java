package com.faforever.server.map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface Ladder1v1MapRepository extends JpaRepository<MapVersion, Integer> {
  @Query("SELECT game.mapVersion FROM Game game JOIN game.playerStats playerStats WHERE playerStats.player.id=?0 AND game.featuredMod.id=?1 ORDER BY game.startTime DESC")
  Page<MapVersion> findRecentlyPlayedLadderMapVersions(Pageable pageable, int playerId, int ladderModId);
}
