package com.faforever.server.map;

import com.faforever.server.entity.MapVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Ladder1v1MapRepository extends JpaRepository<MapVersion, Integer> {

}
