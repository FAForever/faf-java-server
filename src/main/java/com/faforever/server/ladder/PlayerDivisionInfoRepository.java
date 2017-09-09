package com.faforever.server.ladder;

import com.faforever.server.entity.Player;
import com.faforever.server.entity.PlayerDivisionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerDivisionInfoRepository extends JpaRepository<PlayerDivisionInfo, Integer> {
  PlayerDivisionInfo findByPlayerAndSeason(Player player, int season);
}
