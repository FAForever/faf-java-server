package com.faforever.server.coop;

import com.faforever.server.error.ErrorCode;
import com.faforever.server.game.Game;
import com.faforever.server.game.GamePlayerStats;
import com.faforever.server.game.Validity;
import com.faforever.server.player.Player;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.faforever.server.error.RequestExceptionWithCode.requestExceptionWithCode;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CoopServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private CoopService instance;
  @Mock
  private CoopMapRepository coopMapRepository;
  @Mock
  private CoopLeaderboardRepository coopLeaderboardRepository;

  @Before
  public void setUp() throws Exception {
    instance = new CoopService(coopMapRepository, coopLeaderboardRepository);
  }

  @Test
  public void reportOperationCompleteNotInGame() throws Exception {
    expectedException.expect(requestExceptionWithCode(ErrorCode.COOP_CANT_REPORT_NOT_IN_GAME));
    instance.reportOperationComplete(new Player(), true, Duration.ofMinutes(1));
  }

  @Test
  public void reportOperationCompleteNotValid() throws Exception {
    /* Coop games cannot have state VALID. Should be COOP_UNRANKED
    *   for it to show up in the leaderboard.
    */
    Game game = new Game()
                  .setId(42)
                  .setMapFolderName("SCMP_001")
                  .setValidity(Validity.VALID);

    Player player = new Player();
    player.setCurrentGame(game);

    /* This needs to exist in order for the test to fail in case the validity
     * check is removed. For the test to pass however, we need to allow this
     * unused mock by using the Silent runner. There are ways to just enable
     * lenient stubbing on a single mock, but they don't seem to work with the
     * current project configurations.
     */
    CoopMap mission = new CoopMap();
    when(coopMapRepository.findOneByFilenameLikeIgnoreCase("SCMP_001")).thenReturn(Optional.of(mission));

    instance.reportOperationComplete(player, false, Duration.ofMinutes(8));
    verifyZeroInteractions(coopLeaderboardRepository);
  }

  @Test
  public void reportOperationComplete() throws Exception {
    Game game = new Game()
                  .setId(42)
                  .setMapFolderName("SCMP_001")
                  .setValidity(Validity.COOP_UNRANKED);
    Map<Integer, GamePlayerStats> playerStats = game.getPlayerStats();
    playerStats.put(1, new GamePlayerStats());
    playerStats.put(2, new GamePlayerStats());
    playerStats.put(3, new GamePlayerStats());

    Player player = new Player();
    player.setCurrentGame(game);

    CoopMap mission = new CoopMap();
    when(coopMapRepository.findOneByFilenameLikeIgnoreCase("SCMP_001")).thenReturn(Optional.of(mission));

    instance.reportOperationComplete(player, false, Duration.ofMinutes(8));

    ArgumentCaptor<CoopLeaderboardEntry> captor = ArgumentCaptor.forClass(CoopLeaderboardEntry.class);
    verify(coopLeaderboardRepository).save(captor.capture());
    CoopLeaderboardEntry entry = captor.getValue();

    assertThat(entry.getGameId(), is(42L));
    assertThat(entry.getMission(), is(mission));
    assertThat(entry.getPlayerCount(), is(3));
    assertThat(entry.getTime(), is(Time.from(Instant.EPOCH.plus(Duration.ofMinutes(8)))));
  }

  @Test
  public void getMaps() throws Exception {
    List<CoopMap> maps = Arrays.asList(new CoopMap(), new CoopMap());
    when(coopMapRepository.findAll()).thenReturn(maps);

    List<CoopMap> result = instance.getMaps();

    assertThat(result, is(maps));
  }
}
