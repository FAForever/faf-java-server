package com.faforever.server.api;

import com.faforever.server.api.dto.UpdatedAchievement;
import com.faforever.server.config.FafServerProperties;
import com.faforever.server.stats.achievements.AchievementUpdate;
import com.faforever.server.stats.event.EventUpdate;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.Arrays.asList;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class ApiAccessor {

  private final OAuth2RestOperations restOperations;
  private final FafServerProperties fafServerProperties;

  public ApiAccessor(OAuth2RestOperations restOperations, FafServerProperties fafServerProperties) {
    this.restOperations = restOperations;
    this.fafServerProperties = fafServerProperties;
  }

  public CompletionStage<List<UpdatedAchievement>> updateAchievements(List<AchievementUpdate> achievementUpdates) {
    return supplyAsync(() -> asList(restOperations.patchForObject(url("/player_achievements"), achievementUpdates, UpdatedAchievement[].class)));
  }

  public void updateEvents(List<EventUpdate> eventUpdates) {
    supplyAsync(() -> Collections.singletonList(restOperations.patchForObject(url("/player_events"), eventUpdates, Void.class)));
  }

  private String url(String route) {
    return fafServerProperties.getApiBaseUrl() + route;
  }
}
