package com.faforever.server.api.dto;

import com.faforever.server.stats.achievements.AchievementUpdate;
import com.faforever.server.stats.achievements.AchievementUpdate.UpdateType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AchievementUpdateRequest {

  private final int playerId;
  private final String achievementId;
  private final Operation operation;
  private final int steps;

  public static AchievementUpdateRequest fromInternal(AchievementUpdate achievementUpdate) {
    return new AchievementUpdateRequest(
      achievementUpdate.getPlayerId(),
      achievementUpdate.getAchievementId().getId(),
      Operation.fromUpdateType(achievementUpdate.getUpdateType()),
      achievementUpdate.getSteps()
    );
  }

  public enum Operation {
    REVEAL, UNLOCK, INCREMENT, SET_STEPS_AT_LEAST;

    public static Operation fromUpdateType(UpdateType updateType) {
      return valueOf(updateType.name());
    }
  }
}
