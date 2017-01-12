package com.faforever.server.mod;

import com.faforever.server.entity.ModVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModRepository extends JpaRepository<ModVersion, Integer> {

  Optional<ModVersion> findOneByUidAndRankedTrue(String uid);
}
