package com.faforever.server.map;

import com.faforever.server.cache.CacheNames;
import com.faforever.server.entity.MapFeatures;
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
  private MapFeaturesRepository mapFeaturesRepository;
  private final Random random;

  public MapService(MapVersionRepository mapVersionRepository,
                    Ladder1v1MapRepository ladder1v1MapRepository,
                    MapFeaturesRepository mapFeaturesRepository) {
    this.mapVersionRepository = mapVersionRepository;
    this.ladder1v1MapRepository = ladder1v1MapRepository;
    this.mapFeaturesRepository = mapFeaturesRepository;
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
  public void incrementTimesPlayed(MapVersion map) {
    MapFeatures features = getMapFeatures(map);
    features.incrementTimesPlayed();
    mapFeaturesRepository.save(features);
  }

  public MapFeatures getMapFeatures(MapVersion map){
    MapFeatures features = mapFeaturesRepository.findOne(map.getId());
    if(features == null){
      features = new MapFeatures().setId(map.getId());
      mapFeaturesRepository.save(features);
    }
    return features;
  }

  private List<MapVersion> getLadder1v1Maps() {
    return ladder1v1MapRepository.findAll();
  }
}
