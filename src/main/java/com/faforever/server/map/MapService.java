package com.faforever.server.map;

import com.faforever.server.entity.MapVersion;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MapService {
  private final MapVersionRepository mapVersionRepository;
  private final Ladder1v1MapRepository ladder1v1MapRepository;
  private final Random random;

  public MapService(MapVersionRepository mapVersionRepository, Ladder1v1MapRepository ladder1v1MapRepository) {
    this.mapVersionRepository = mapVersionRepository;
    this.ladder1v1MapRepository = ladder1v1MapRepository;
    this.random = new Random();
  }

  public Optional<MapVersion> findMap(String filename) {
    return mapVersionRepository.findByFilenameIgnoreCase(filename);
  }

  public boolean isBlacklisted(String mapname) {
    return false;//TODO: implement
  }

  public MapVersion getRandomLadderMap() {
    List<MapVersion> ladder1v1Maps = getLadder1v1Maps();
    return ladder1v1Maps.get(random.nextInt(ladder1v1Maps.size() - 1));
  }

  private List<MapVersion> getLadder1v1Maps() {
    return ladder1v1MapRepository.findAll();
  }
}
