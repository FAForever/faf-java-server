package com.faforever.server.map;

import com.faforever.server.entity.MapFeatures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapFeaturesRepository extends JpaRepository<MapFeatures, Integer> {
}
