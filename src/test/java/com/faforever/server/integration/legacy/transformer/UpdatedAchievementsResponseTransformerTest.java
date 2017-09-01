package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.api.dto.AchievementState;
import com.faforever.server.client.UpdatedAchievementsResponse;
import com.faforever.server.client.UpdatedAchievementsResponse.UpdatedAchievement;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UpdatedAchievementsResponseTransformerTest {
  @Test
  @SuppressWarnings("unchecked")
  public void transform() throws Exception {
    Map<String, Serializable> result = UpdatedAchievementsResponseTransformer.INSTANCE.transform(new UpdatedAchievementsResponse(Arrays.asList(
      new UpdatedAchievement("111", 1, AchievementState.REVEALED, false),
      new UpdatedAchievement("222", 10, AchievementState.REVEALED, true)
    )));

    assertThat(result.get("command"), is("updated_achievements"));

    assertThat((List<Map<String, Serializable>>) result.get("updated_achievements"), Matchers.containsInAnyOrder(
      ImmutableMap.of(
        "achievement_id", "111",
        "current_state", AchievementState.REVEALED,
        "current_steps", 1,
        "newly_unlocked", false
      ),
      ImmutableMap.of(
        "achievement_id", "222",
        "current_state", AchievementState.REVEALED,
        "current_steps", 10,
        "newly_unlocked", true
      )
    ));
  }
}
