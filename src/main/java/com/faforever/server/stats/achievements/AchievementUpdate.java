package com.faforever.server.stats.achievements;

import lombok.Data;

@Data
public class AchievementUpdate {
  private final AchievementId achievementId;
  private final UpdateType updateType;
  private final int steps;

  public enum UpdateType {
    REVEAL, INCREMENT, UNLOCK, SET_STEPS_AT_LEAST
  }
}
