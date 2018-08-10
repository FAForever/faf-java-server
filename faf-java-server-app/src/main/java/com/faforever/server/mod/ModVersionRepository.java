package com.faforever.server.mod;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModVersionRepository extends JpaRepository<ModVersion, Integer> {

  Optional<ModVersion> findOneByUidAndRankedTrue(String uid);

  List<ModVersion> findByUidIn(List<String> uids);
}
