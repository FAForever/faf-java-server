package com.faforever.server.mod;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface FeaturedModRepository extends JpaRepository<FeaturedMod, Integer> {
  Optional<FeaturedMod> findOneByTechnicalName(String technicalName);
}
