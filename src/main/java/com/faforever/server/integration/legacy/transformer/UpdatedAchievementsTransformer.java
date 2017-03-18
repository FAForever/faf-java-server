package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.UpdatedAchievementsResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public enum UpdatedAchievementsTransformer implements GenericTransformer<UpdatedAchievementsResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(UpdatedAchievementsResponse source) {
    return ImmutableMap.of(
      "command", "updated_achievements",
      "updated_achievements", source.getUpdatedAchievements().stream()
        .map(updatedAchievement -> ImmutableMap.of(
          "achievement_id", updatedAchievement.getAchievementId(),
          "current_state", updatedAchievement.getCurrentState(),
          "current_steps", updatedAchievement.getCurrentSteps(),
          "newly_unlocked", updatedAchievement.isNewlyUnlocked()
        ))
        .collect(Collectors.toCollection(ArrayList::new))
    );
  }

}
