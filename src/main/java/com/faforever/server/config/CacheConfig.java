package com.faforever.server.config;

import com.faforever.server.cache.CacheNames;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.concurrent.TimeUnit.MINUTES;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(Arrays.asList(
      new GuavaCache(CacheNames.FEATURED_MODS, newBuilder().expireAfterWrite(10, MINUTES).build()),
      new GuavaCache(CacheNames.RANKED_MODS, newBuilder().expireAfterWrite(10, MINUTES).build())
    ));
    return cacheManager;
  }
}
