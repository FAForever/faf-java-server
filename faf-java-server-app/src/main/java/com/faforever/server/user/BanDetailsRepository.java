package com.faforever.server.user;

import com.faforever.server.entity.BanDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BanDetailsRepository extends JpaRepository<BanDetails, String> {

}
