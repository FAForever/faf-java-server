package com.faforever.server.map;

import com.faforever.server.cache.CacheNames;
import com.faforever.server.entity.MapVersion;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class MapService {
  private final MapVersionRepository mapVersionRepository;
  private final Ladder1v1MapRepository ladder1v1MapRepository;
  private final Random random;

  public MapService(MapVersionRepository mapVersionRepository, Ladder1v1MapRepository ladder1v1MapRepository) {
    this.mapVersionRepository = mapVersionRepository;
    this.ladder1v1MapRepository = ladder1v1MapRepository;
    this.random = new Random();
  }

  @Cacheable(CacheNames.MAP_VERSIONS)
  public Optional<MapVersion> findMap(String filename) {
    return mapVersionRepository.findByFilenameIgnoreCase(filename);
  }

  public MapVersion getRandomLadderMap() {
    List<MapVersion> ladder1v1Maps = getLadder1v1Maps();
    return ladder1v1Maps.get(random.nextInt(ladder1v1Maps.size() - 1));
  }

  @Transactional
  public void increaseTimesPlayed(String filename) {
    // pulling it from the repository again in case the given map is not up to date
    Optional<MapVersion> mapFromRepository = findMap(filename);
    if(mapFromRepository.isPresent()){
      mapFromRepository.get().getFeatures().incrementTimesPlayed();
      mapVersionRepository.save(mapFromRepository.get());
    }
  }

  private List<MapVersion> getLadder1v1Maps() {
    return ladder1v1MapRepository.findAll();
  }
}
