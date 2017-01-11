package com.faforever.server.api.dto;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class UpdatedAchievement {

  private final Integer currentSteps;
  private final AchievementState state;
  private final boolean newlyUnlocked;

  public UpdatedAchievement(boolean newlyUnlocked, AchievementState state) {
    this(newlyUnlocked, state, null);
  }

  UpdatedAchievement(boolean newlyUnlocked, AchievementState state, @Nullable Integer currentSteps) {
    this.currentSteps = currentSteps;
    this.state = state;
    this.newlyUnlocked = newlyUnlocked;
  }
}
