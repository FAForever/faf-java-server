package com.faforever.server.config;

import com.faforever.server.game.ActiveGameRepository;
import com.faforever.server.player.OnlinePlayerRepository;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(excludeFilters = @Filter(
  type = FilterType.ASSIGNABLE_TYPE,
  classes = {
    ActiveGameRepository.class,
    OnlinePlayerRepository.class
  }),
  basePackages = "com.faforever.server"
)
@Configuration
public class JpaRepositoryConfig {
}
