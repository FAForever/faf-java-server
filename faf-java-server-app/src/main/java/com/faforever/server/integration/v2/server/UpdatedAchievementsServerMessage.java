package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Message sent from the server to the client to inform when the player's achievement progress has been updated.
 */
@Getter
@Setter
@V2ServerResponse
class UpdatedAchievementsServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "updatedAchievements";

  /** The list of updated achievements. */
  @NotNull
  private List<UpdatedAchievement> updatedAchievements;

  enum AchievementState {
    /** The achievement is visible to the player. */
    REVEALED,
    /** The achievement has been unlocked by the player. */
    UNLOCKED,
    /** The achievement is hidden to the player. */
    HIDDEN
  }

  /** The result of an achievement update. */
  @Getter
  @Setter
  static class UpdatedAchievement {
    /** The ID of the achievement that has been updated. */
    @NotNull
    String achievementId;

    /**
     * The current achievement steps (e.g. 5 out of 10) that have been performed. {@code null} if the achievement does
     * not define steps.
     */
    Integer currentSteps;

    /** Current state of the achievement. */
    @NotNull
    AchievementState currentState;

    /** ({@code true}) if the achievement has just been unlocked, ({@code true}) otherwise. */
    boolean newlyUnlocked;
  }
}
