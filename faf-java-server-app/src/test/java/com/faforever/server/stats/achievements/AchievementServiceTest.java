package com.faforever.server.stats.achievements;

import com.faforever.server.api.ApiAccessor;
import com.faforever.server.stats.achievements.AchievementUpdate.UpdateType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AchievementServiceTest {

  private AchievementService instance;

  @Mock
  private ApiAccessor apiAccessor;

  @Before
  public void setUp() throws Exception {
    instance = new AchievementService(apiAccessor);
  }

  @Test
  public void executeBatchUpdate() throws Exception {
    List<AchievementUpdate> achievementUpdates = Arrays.asList(
      new AchievementUpdate(1, AchievementId.ACH_ADDICT, UpdateType.INCREMENT, 1),
      new AchievementUpdate(1, AchievementId.ACH_AURORA, UpdateType.INCREMENT, 1)
    );

    instance.executeBatchUpdate(achievementUpdates);

    verify(apiAccessor).updateAchievements(achievementUpdates);
  }
}
