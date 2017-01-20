package com.faforever.server.mod;

import com.faforever.server.entity.FeaturedMod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeaturedModRepository extends JpaRepository<FeaturedMod, Integer> {
  Optional<FeaturedMod> findOneByTechnicalName(String technicalName);
}
