package com.faforever.server.api.dto;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Type("updatedAchievement")
@Getter
@Setter
@NoArgsConstructor
public class UpdatedAchievementResponse {

  @Id
  private String id;
  private String achievementId;
  private Integer currentSteps;
  private AchievementState state;
  private boolean newlyUnlocked;

  public UpdatedAchievementResponse(String achievementId, boolean newlyUnlocked, AchievementState state) {
    this.achievementId = achievementId;
    this.state = state;
    this.newlyUnlocked = newlyUnlocked;
  }
}
