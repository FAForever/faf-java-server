package com.faforever.server.teamkill;

import com.faforever.server.entity.Game;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.TeamKill;
import com.faforever.server.player.PlayerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
// TODO verify logger calls
public class TeamKillServiceTest {

  @Mock
  private PlayerService playerService;
  @Mock
  private TeamKillRepository teamKillRepository;

  private TeamKillService instance;

  @Before
  public void setUp() throws Exception {
    when(playerService.getPlayer(anyInt())).thenReturn(Optional.empty());

    instance = new TeamKillService(playerService, teamKillRepository);
  }

  @Test
  public void reportTeamKill() throws Exception {
    Game game = new Game();
    game.setId(10);

    Player player = new Player();
    player.setCurrentGame(game);
    player.setId(1);

    Player killer = new Player();
    player.setId(2);

    when(playerService.getPlayer(player.getId())).thenReturn(Optional.of(player));
    when(playerService.getPlayer(killer.getId())).thenReturn(Optional.of(killer));

    instance.reportTeamKill(player, Duration.ofMinutes(28), killer.getId(), player.getId());

    ArgumentCaptor<TeamKill> captor = ArgumentCaptor.forClass(TeamKill.class);
    verify(teamKillRepository).save(captor.capture());
    TeamKill teamKill = captor.getValue();

    assertThat(teamKill.getGameId(), is(game.getId()));
    assertThat(teamKill.getTeamKiller(), is(killer.getId()));
    assertThat(teamKill.getVictim(), is(player.getId()));
    assertThat(teamKill.getGameTime(), is((int) Duration.ofMinutes(28).getSeconds()));
    assertThat(teamKill.getReportedAt().after(Timestamp.from(Instant.now().minusSeconds(10))), is(true));
  }

  @Test
  public void reportTeamKillWithoutGame() throws Exception {
    Player player = new Player();
    player.setId(1);

    Player killer = new Player();
    player.setId(2);

    when(playerService.getPlayer(player.getId())).thenReturn(Optional.of(player));
    when(playerService.getPlayer(killer.getId())).thenReturn(Optional.of(killer));

    instance.reportTeamKill(player, Duration.ofMinutes(28), killer.getId(), player.getId());
    verifyZeroInteractions(teamKillRepository);
  }

  @Test
  public void reportTeamKillWhenKillerDoesntExist() throws Exception {
    Player player = new Player();
    player.setCurrentGame(new Game());
    player.setId(1);

    when(playerService.getPlayer(player.getId())).thenReturn(Optional.of(player));

    instance.reportTeamKill(player, Duration.ofMinutes(28), 991234, player.getId());
    verifyZeroInteractions(teamKillRepository);
  }

  @Test
  public void reportTeamKillWhenVictimIsntReported() throws Exception {
    Player reporter = new Player();
    reporter.setCurrentGame(new Game());
    reporter.setId(1);

    Player killer = new Player();
    killer.setId(2);

    Player victim = new Player();
    killer.setId(2);

    when(playerService.getPlayer(killer.getId())).thenReturn(Optional.of(killer));

    instance.reportTeamKill(reporter, Duration.ofMinutes(28), killer.getId(), victim.getId());
    verifyZeroInteractions(teamKillRepository);
  }
}
