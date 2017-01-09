package com.faforever.server.map;

import com.faforever.server.entity.MapVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Optional;

@Repository
public interface MapVersionRepository extends JpaRepository<MapVersion, Integer> {
  Optional<MapVersion> findByFilenameIgnoreCase(String mapName);
}
