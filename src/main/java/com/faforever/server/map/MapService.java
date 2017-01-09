package com.faforever.server.map;

import com.faforever.server.entity.MapVersion;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MapService {
  private final MapVersionRepository mapVersionRepository;

  public MapService(MapVersionRepository mapVersionRepository) {
    this.mapVersionRepository = mapVersionRepository;
  }

  public Optional<MapVersion> findMap(String filename) {
    return mapVersionRepository.findByFilenameIgnoreCase(filename);
  }
}
