package com.faforever.server.api;

import com.faforever.server.api.dto.AchievementUpdateRequest;
import com.faforever.server.api.dto.EventUpdateRequest;
import com.faforever.server.api.dto.UpdatedAchievementResponse;
import com.faforever.server.api.dto.UpdatedEventResponse;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.stats.achievements.AchievementUpdate;
import com.faforever.server.stats.event.EventUpdate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiAccessor {

  private final ServerProperties serverProperties;
  private final RestOperations restOperations;

  @Inject
  public ApiAccessor(RestOperations restOperations, ServerProperties serverProperties) {
    this.restOperations = restOperations;
    this.serverProperties = serverProperties;
  }

  public List<UpdatedAchievementResponse> updateAchievements(List<AchievementUpdate> achievementUpdates) {
    List<AchievementUpdateRequest> updates = achievementUpdates.stream()
      .map(AchievementUpdateRequest::fromInternal)
      .collect(Collectors.toList());
    return patch("/achievements/update", updates);
  }

  @Async
  public List<UpdatedEventResponse> updateEvents(List<EventUpdate> eventUpdates) {
    List<EventUpdateRequest> updates = eventUpdates.stream()
      .map(EventUpdateRequest::fromInternal)
      .collect(Collectors.toList());
    return patch("/events/update", updates);
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> patch(String endPoint, Object body) {
    return (List<T>) restOperations.patchForObject(url(endPoint), body, List.class);
  }

  private String url(String route) {
    return serverProperties.getApi().getBaseUrl() + route;
  }
}
