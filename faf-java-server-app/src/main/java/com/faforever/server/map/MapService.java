package com.faforever.server.map;

import com.faforever.server.cache.CacheNames;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Map;
import com.faforever.server.entity.MapStats;
import com.faforever.server.entity.MapVersion;
import com.faforever.server.entity.Player;
import com.faforever.server.mod.ModService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class MapService {
  private final MapVersionRepository mapVersionRepository;
  private final Ladder1v1MapRepository ladder1v1MapRepository;
  private final MapStatsRepository mapStatsRepository;
  private final Random random;
  private final ServerProperties serverProperties;
  private final ModService modService;

  public MapService(MapVersionRepository mapVersionRepository,
                    Ladder1v1MapRepository ladder1v1MapRepository,
                    MapStatsRepository mapFeaturesRepository,
                    ServerProperties serverProperties,
                    ModService modService) {
    this.mapVersionRepository = mapVersionRepository;
    this.ladder1v1MapRepository = ladder1v1MapRepository;
    this.mapStatsRepository = mapFeaturesRepository;
    this.serverProperties = serverProperties;
    this.modService = modService;
    this.random = new Random();
  }

  @Cacheable(CacheNames.MAP_VERSIONS)
  public Optional<MapVersion> findMap(String filename) {
    return mapVersionRepository.findByFilenameIgnoreCase(filename);
  }

  @Cacheable(CacheNames.MAP_VERSIONS)
  public Optional<MapVersion> findMap(int mapVersionId) {
    return mapVersionRepository.findById(mapVersionId);
  }

  public MapVersion getRandomLadderMap(Iterable<Player> players) {
    List<MapVersion> originalLadderMapPool = getLadder1v1Maps();
    List<MapVersion> modifiedLadderMapPool = new ArrayList<>(originalLadderMapPool);

    int lastMapsNotConsidered = serverProperties.getLadder1v1().getLastMapsNotConsidered();

    for (Player player : players) {
      modifiedLadderMapPool.removeAll(getRecentlyPlayedLadderMapVersions(player, lastMapsNotConsidered));
    }

    if (modifiedLadderMapPool.size() > 1) {
      return modifiedLadderMapPool.get(random.nextInt(modifiedLadderMapPool.size() - 1));
    }
    return originalLadderMapPool.get(random.nextInt(originalLadderMapPool.size() - 1));
  }

  @Transactional
  public List<MapVersion> getRecentlyPlayedLadderMapVersions(Player player, int limit) {
    Optional<FeaturedMod> ladder1v1 = modService.getLadder1v1Mod();
    if (!ladder1v1.isPresent()) {
      throw new IllegalStateException("Ladder 1v1 mod could not be found");
    }
    return ladder1v1MapRepository.findRecentlyPlayedLadderMapVersions(new PageRequest(0, limit), player.getId(), ladder1v1.get().getId()).getContent();
  }

  @Transactional
  public void incrementTimesPlayed(Map map) {
    MapStats features = getMapStats(map);
    features.incrementTimesPlayed();
    mapStatsRepository.save(features);
  }

  @Transactional
  public MapStats getMapStats(Map map) {
    return mapStatsRepository.findById(map.getId())
      .orElseGet(() -> {
        MapStats mapStats = new MapStats().setId(map.getId());
        return mapStatsRepository.save(mapStats);
      });
  }

  private List<MapVersion> getLadder1v1Maps() {
    return ladder1v1MapRepository.findAll();
  }
}
