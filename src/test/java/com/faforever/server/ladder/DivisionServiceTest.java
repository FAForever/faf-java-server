package com.faforever.server.ladder;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.Division;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.PlayerDivisionInfo;
import com.googlecode.zohhak.api.TestWith;
import com.googlecode.zohhak.api.runners.ZohhakRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(ZohhakRunner.class)
public class DivisionServiceTest {
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private DivisionService instance;

  @Mock
  private DivisionRepository divisionRepository;
  @Mock
  private PlayerDivisionInfoRepository playerDivisionInfoRepository;

  @Mock
  private Player player1;
  @Mock
  private Player player2;

  private PlayerDivisionInfo info1;
  private PlayerDivisionInfo info2;

  private ServerProperties serverProperties;


  @Before
  public void setUp() throws Exception {
    serverProperties = new ServerProperties();
    serverProperties.getLadder().setSeason(1);

    info1 = new PlayerDivisionInfo();
    info1.setPlayer(player1);
    info1.setSeason(serverProperties.getLadder().getSeason());

    info2 = new PlayerDivisionInfo();
    info2.setPlayer(player2);
    info2.setSeason(serverProperties.getLadder().getSeason());

    Division[] divisions = new Division[]{
      createDivision(1, "League 1 - Division A", 1, 10),
      createDivision(2, "League 1 - Division B", 1, 30),
      createDivision(3, "League 1 - Division C", 1, 50),
      createDivision(4, "League 2 - Division D", 2, 20),
      createDivision(5, "League 2 - Division E", 2, 60),
      createDivision(6, "League 2 - Division F", 2, 100),
      createDivision(7, "League 3 - Division G", 3, 100),
      createDivision(8, "League 3 - Division H", 3, 200),
      createDivision(9, "League 3 - Division I", 3, 9999)
    };

    when(divisionRepository.findAllByOrderByLeagueAscThresholdAsc()).thenReturn(Arrays.asList(divisions));

    instance = new DivisionService(serverProperties, divisionRepository, playerDivisionInfoRepository);
    instance.init();
  }

  @TestWith({
    "1,   0.0f,  1",
    "1,   1.0f,  1",
    "1,  10.0f,  1",
    "1,  10.1f,  2",
    "1,  30.0f,  2",
    "2,  10.0f,  4",
    "2,  99.9f,  6",
    "3, 999.0f,  9"
  })
  public void testGetCurrentPlayerDivision(int league, float score, int expectedDivisionId) throws Exception {
    PlayerDivisionInfo info = new PlayerDivisionInfo();
    info.setPlayer(player1);
    info.setLeague(league);
    info.setScore(score);

    when(playerDivisionInfoRepository.findByPlayerAndSeason(player1, serverProperties.getLadder().getSeason())).thenReturn(info);

    Division result = instance.getCurrentPlayerDivision(player1).get();

    assertEquals(result.getId(), expectedDivisionId);
  }

  @Test(expected = IllegalStateException.class)
  public void testGetCurrentPlayerDivisionForInvalidScore() throws Exception {
    PlayerDivisionInfo info = new PlayerDivisionInfo();
    info.setPlayer(player1);
    info.setLeague(1);
    info.setScore(99999);

    when(playerDivisionInfoRepository.findByPlayerAndSeason(player1, serverProperties.getLadder().getSeason())).thenReturn(info);

    instance.getCurrentPlayerDivision(player1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPostResultWithInvalidWinner() throws Exception {
    instance.postResult(player1, player2, mock(Player.class));
  }

  @Test
  public void testPostResultWithNewPlayers() throws Exception {

    when(playerDivisionInfoRepository.findByPlayerAndSeason(player1, serverProperties.getLadder().getSeason())).thenReturn(null);
    when(playerDivisionInfoRepository.findByPlayerAndSeason(player2, serverProperties.getLadder().getSeason())).thenReturn(null);

    instance.postResult(player1, player2, player1);

    verify(playerDivisionInfoRepository, times(2)).save(any(PlayerDivisionInfo.class));
  }

  @TestWith({
    // player1 won - player 1 start #games #league #score - player 2 start #games #league #score - player 1 desired #games #league #score - player 2 desired #games #league #score
    "true ,  0, 1, 000.0f,   0, 1, 000.0f,    1, 1, 001.0f,     1, 1, 000.0f", // player one wins, both have no previous score
    "false,  0, 1, 000.0f,   0, 1, 000.0f,    1, 1, 000.0f,     1, 1, 001.0f", // player two wins, both have no previous score
    "true ,  5, 1, 010.0f,   0, 1, 010.0f,    6, 1, 011.0f,     1, 1, 009.5f", // player one wins, both in same league
    "false,  5, 1, 010.0f,   0, 1, 010.0f,    6, 1, 009.5f,     1, 1, 011.0f", // player two wins, both in same league
    "true ,  0, 1, 010.0f,   0, 2, 010.0f,    1, 1, 011.5f,     1, 2, 009.0f", // player one wins, player one in inferior league
    "false,  0, 2, 010.0f,   0, 1, 010.0f,    1, 2, 009.0f,     1, 1, 011.5f", // player two wins, player two in inferior league
    "true ,  0, 2, 010.0f,   0, 1, 010.0f,    1, 2, 010.5f,     1, 1, 009.5f", // player one wins, player one in superior league
    "false,  0, 1, 010.0f,   0, 2, 010.0f,    1, 1, 009.5f,     1, 2, 010.5f", // player two wins, player two in superior league
    "true ,  5, 1, 049.0f,   0, 1, 049.0f,    6, 1, 050.0f,     1, 1, 048.5f", // player one wins, both in same league - max threshold, no promotion
    "false,  5, 1, 049.0f,   0, 1, 049.0f,    6, 1, 048.5f,     1, 1, 050.0f", // player two wins, both in same league - max threshold, no promotion
    "true ,  5, 1, 050.0f,   0, 1, 050.0f,    6, 2, 000.0f,     1, 1, 049.5f", // player one wins and promoted, both in same league
    "false,  5, 1, 050.0f,   0, 1, 050.0f,    6, 1, 049.5f,     1, 2, 000.0f", // player two wins and promoted, both in same league
  })
  public void testPostResult(boolean p1Won, int p1StartGames, int p1StartLeague, float p1StartScore, int p2StartGames, int p2StartLeague, float p2StartScore, int p1EndGames, int p1EndLeague, float p1EndScore, int p2EndGames, int p2EndLeague, float p2EndScore) throws Exception {
    serverProperties.getLadder().setRegularGain(1.0f);
    serverProperties.getLadder().setRegularLoss(0.5f);
    serverProperties.getLadder().setIncreasedGain(1.5f);
    serverProperties.getLadder().setIncreasedLoss(1.0f);
    serverProperties.getLadder().setReducedGain(0.5f);
    serverProperties.getLadder().setReducedLoss(0.5f);

    info1.setGames(p1StartGames);
    info1.setLeague(p1StartLeague);
    info1.setScore(p1StartScore);
    info2.setGames(p2StartGames);
    info2.setLeague(p2StartLeague);
    info2.setScore(p2StartScore);

    when(playerDivisionInfoRepository.findByPlayerAndSeason(player1, serverProperties.getLadder().getSeason())).thenReturn(info1);
    when(playerDivisionInfoRepository.findByPlayerAndSeason(player2, serverProperties.getLadder().getSeason())).thenReturn(info2);

    instance.postResult(player1, player2, p1Won ? player1 : player2);

    assertEquals(p1EndGames, info1.getGames());
    assertEquals(p1EndLeague, info1.getLeague());
    assertEquals(p1EndScore, info1.getScore(), 0.01f);

    assertEquals(p2EndGames, info2.getGames());
    assertEquals(p2EndLeague, info2.getLeague());
    assertEquals(p2EndScore, info2.getScore(), 0.01f);
  }

  private Division createDivision(int id, String name, int league, int threshold) {
    return new Division()
      .setId(id)
      .setName(name)
      .setLeague(league)
      .setThreshold(threshold);
  }
}
