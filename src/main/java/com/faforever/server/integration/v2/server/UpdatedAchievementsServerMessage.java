package com.faforever.server.integration.v2.server;

import com.faforever.server.api.dto.AchievementState;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Message sent from the server to the client to inform when the player's achievement progress has been updated.
 */
@Getter
@Setter
class UpdatedAchievementsServerMessage extends V2ServerMessage {
  private List<UpdatedAchievement> updatedAchievements;

  @Getter
  @Setter
  static class UpdatedAchievement {
    /** The ID of the achievement that has been updated. */
    String achievementId;
    /**
     * The current achievement steps (e.g. 5 out of 10) that have been performed. {@code null} if the achievement does
     * not define steps.
     */
    Integer currentSteps;
    /** Current state of the achievement. */
    AchievementState currentState;
    /** ({@code true}) if the achievement has just been unlocked, ({@code true}) otherwise. */
    boolean newlyUnlocked;
  }
}
