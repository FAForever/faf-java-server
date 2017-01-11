package com.faforever.server.coop;

import com.faforever.server.entity.CoopMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoopMapRepository extends JpaRepository<CoopMap, Integer> {

  Optional<CoopMap> findOneByFilenameLikeIgnoreCase(String filename);
}
