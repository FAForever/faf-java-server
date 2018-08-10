package com.faforever.server.config;

import com.faforever.server.game.ActiveGameRepository;
import com.faforever.server.player.OnlinePlayerRepository;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.map.repository.config.EnableMapRepositories;

@EnableMapRepositories(includeFilters = @Filter(
  type = FilterType.ASSIGNABLE_TYPE,
  classes = {
    ActiveGameRepository.class,
    OnlinePlayerRepository.class
  }),
  basePackages = "com.faforever.server"
)
@Configuration
public class MapRepositoryConfig {
}
