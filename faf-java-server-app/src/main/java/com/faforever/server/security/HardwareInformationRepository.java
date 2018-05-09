package com.faforever.server.security;

import com.faforever.server.entity.HardwareInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HardwareInformationRepository extends JpaRepository<HardwareInformation, Integer> {

  Optional<HardwareInformation> findOneByHash(String hash);
}
