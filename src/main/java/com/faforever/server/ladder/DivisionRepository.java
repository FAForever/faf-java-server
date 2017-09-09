package com.faforever.server.ladder;

import com.faforever.server.entity.Division;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Integer> {
  public List<Division> findAllByOrderByLeagueAscThresholdAsc();
}
