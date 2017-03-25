package com.faforever.server.stats.achievements;

import com.faforever.server.api.ApiAccessor;
import com.faforever.server.api.dto.UpdatedAchievementResponse;
import com.faforever.server.entity.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AchievementService {

  private final ApiAccessor apiAccessor;

  public AchievementService(ApiAccessor apiAccessor) {
    this.apiAccessor = apiAccessor;
  }

  @Async
  public CompletableFuture<List<UpdatedAchievementResponse>> executeBatchUpdate(Player player, List<AchievementUpdate> achievementUpdates) {
    log.debug("Updating '{}' achievements for player '{}'", achievementUpdates.size(), player);
    return CompletableFuture.completedFuture(apiAccessor.updateAchievements(achievementUpdates));
  }
}
