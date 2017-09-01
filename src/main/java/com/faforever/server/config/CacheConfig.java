package com.faforever.server.config;

import com.faforever.server.cache.CacheNames;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static java.util.concurrent.TimeUnit.MINUTES;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(Arrays.asList(
      new CaffeineCache(CacheNames.FEATURED_MODS, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(CacheNames.RANKED_MODS, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(CacheNames.MAP_VERSIONS, newBuilder().expireAfterAccess(5, MINUTES).build())
    ));
    return cacheManager;
  }
}
