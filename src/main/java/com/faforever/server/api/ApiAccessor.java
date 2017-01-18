package com.faforever.server.api;

import com.faforever.server.api.dto.UpdatedAchievement;
import com.faforever.server.config.ServerProperties;
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
  private final ServerProperties serverProperties;

  public ApiAccessor(OAuth2RestOperations restOperations, ServerProperties serverProperties) {
    this.restOperations = restOperations;
    this.serverProperties = serverProperties;
  }

  public CompletionStage<List<UpdatedAchievement>> updateAchievements(List<AchievementUpdate> achievementUpdates) {
    return supplyAsync(() -> asList(restOperations.patchForObject(url("/player_achievements"), achievementUpdates, UpdatedAchievement[].class)));
  }

  public void updateEvents(List<EventUpdate> eventUpdates) {
    supplyAsync(() -> Collections.singletonList(restOperations.patchForObject(url("/player_events"), eventUpdates, Void.class)));
  }

  private String url(String route) {
    return serverProperties.getApiBaseUrl() + route;
  }
}
