package com.faforever.server.rating;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.GamePlayerStats;
import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.Ladder1v1Rating;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.Rating;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class RatingServiceTest {
  private static final int NO_TEAM_ID = 1;
  private RatingService instance;

  @Before
  public void setUp() throws Exception {
    instance = new RatingService(new ServerProperties());
  }

  @Test
  public void initGlobalRating() throws Exception {
    Player player = new Player();
    assertThat(player.getGlobalRating(), is(nullValue()));

    instance.initGlobalRating(player);

    assertThat(player.getGlobalRating(), is(notNullValue()));
    assertThat(player.getGlobalRating().getMean(), is(Matchers.greaterThan(0d)));
    assertThat(player.getGlobalRating().getDeviation(), is(Matchers.greaterThan(0d)));
  }

  @Test
  public void initLadder1v1Rating() throws Exception {
    Player player = new Player();
    assertThat(player.getLadder1v1Rating(), is(nullValue()));

    instance.initLadder1v1Rating(player);

    assertThat(player.getLadder1v1Rating(), is(notNullValue()));
    assertThat(player.getLadder1v1Rating().getMean(), is(Matchers.greaterThan(0d)));
    assertThat(player.getLadder1v1Rating().getDeviation(), is(Matchers.greaterThan(0d)));
  }

  @Test
  public void updateGlobalRatings() throws Exception {
    Player player1 = (Player) new Player()
      .setGlobalRating((GlobalRating) new GlobalRating().setMean(1500d).setDeviation(500d))
      .setId(1);

    Player player2 = (Player) new Player()
      .setGlobalRating((GlobalRating) new GlobalRating().setMean(1500d).setDeviation(500d))
      .setId(2);

    List<GamePlayerStats> playerStats = Arrays.asList(
      new GamePlayerStats()
        .setPlayer(player1)
        .setTeam(NO_TEAM_ID)
        .setMean(player1.getGlobalRating().getMean())
        .setDeviation(player1.getGlobalRating().getDeviation())
        .setScore(10),
      new GamePlayerStats()
        .setPlayer(player2)
        .setTeam(NO_TEAM_ID)
        .setMean(player2.getGlobalRating().getMean())
        .setDeviation(player2.getGlobalRating().getDeviation())
        .setScore(-1)
    );

    instance.updateRatings(playerStats, NO_TEAM_ID, RatingType.GLOBAL);

    assertThat(player1.getGlobalRating().getMean(), is(1765.511882354831));
    assertThat(player1.getGlobalRating().getDeviation(), is(429.1918779825801));

    assertThat(player2.getGlobalRating().getMean(), is(1234.4881176451688));
    assertThat(player2.getGlobalRating().getDeviation(), is(429.1918779825801));

    assertThat(player1.getLadder1v1Rating(), is(nullValue()));
    assertThat(player2.getLadder1v1Rating(), is(nullValue()));
  }

  @Test
  public void updateLadder1v1Ratings() throws Exception {
    Player player1 = (Player) new Player()
      .setLadder1v1Rating((Ladder1v1Rating) new Ladder1v1Rating().setMean(1500d).setDeviation(500d))
      .setId(1);

    Player player2 = (Player) new Player()
      .setLadder1v1Rating((Ladder1v1Rating) new Ladder1v1Rating().setMean(1500d).setDeviation(500d))
      .setId(2);

    List<GamePlayerStats> playerStats = Arrays.asList(
      new GamePlayerStats()
        .setPlayer(player1)
        .setTeam(NO_TEAM_ID)
        .setMean(player1.getLadder1v1Rating().getMean())
        .setDeviation(player1.getLadder1v1Rating().getDeviation())
        .setScore(10),
      new GamePlayerStats()
        .setPlayer(player2)
        .setTeam(NO_TEAM_ID)
        .setMean(player2.getLadder1v1Rating().getMean())
        .setDeviation(player2.getLadder1v1Rating().getDeviation())
        .setScore(-1)
    );

    instance.updateRatings(playerStats, NO_TEAM_ID, RatingType.LADDER_1V1);

    assertThat(player1.getLadder1v1Rating().getMean(), is(1765.511882354831));
    assertThat(player1.getLadder1v1Rating().getDeviation(), is(429.1918779825801));

    assertThat(player2.getLadder1v1Rating().getMean(), is(1234.4881176451688));
    assertThat(player2.getLadder1v1Rating().getDeviation(), is(429.1918779825801));

    assertThat(player1.getGlobalRating(), is(nullValue()));
    assertThat(player2.getGlobalRating(), is(nullValue()));
  }

  @Test
  public void calculateQuality() throws Exception {
    Rating left = new GlobalRating().setMean(1600d).setDeviation(30d);
    Rating right = new GlobalRating().setMean(900d).setDeviation(160d);

    double quality = instance.calculateQuality(left, right);

    assertThat(quality, is(0.16000885216755253));
  }

  @Test
  public void calculateQualityDefaultForNull() throws Exception {
    double quality = instance.calculateQuality(null, null);

    assertThat(quality, is(0.4327310675847713));
  }
}
