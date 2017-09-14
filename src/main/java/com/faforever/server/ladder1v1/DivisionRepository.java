package com.faforever.server.ladder1v1;

import com.faforever.server.entity.Division;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Integer> {
  public List<Division> findAllByOrderByLeagueAscThresholdAsc();
}
