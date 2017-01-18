package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.GamePlayerStats;
import com.faforever.server.entity.GameState;
import com.faforever.server.entity.Player;
import com.faforever.server.game.GameResponse;
import com.faforever.server.game.GameVisibility;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GameResponseTransformerTest {

  private Game game;
  private Player host;

  @Before
  public void setUp() throws Exception {
    host = (Player) new Player().setLogin("Player 1");
    game = new Game()
      .setId(123)
      .setTitle("Test")
      .setFeaturedMod(new FeaturedMod().setTechnicalName("faf"))
      .setMapName("SCMP_001")
      .setHost(host)
      .setMaxPlayers(4)
      .setPlayerStats(Arrays.asList(
        new GamePlayerStats().setTeam(2).setPlayer(host),
        new GamePlayerStats().setTeam(3).setPlayer((Player) new Player().setLogin("Player 2"))
      ));
  }

  @Test
  public void transform() throws Exception {
    game.setState(GameState.OPEN);
    Map<String, Serializable> result = GameResponseTransformer.INSTANCE.transform(new GameResponse(game));

    assertThat(result.get("command"), is("game_info"));
    assertThat(result.get("visibility"), is(GameVisibility.PUBLIC.getString()));
    assertThat(result.get("password_protected"), is(false));
    assertThat(result.get("uid"), is(123));
    assertThat(result.get("title"), is("Test"));
    assertThat(result.get("state"), is("open"));
    assertThat(result.get("featured_mod"), is("faf"));
    assertThat(result.get("featured_mod_versions"), is(ImmutableMap.of()));
    assertThat(result.get("sim_mods"), is(new Object[0]));
    assertThat(result.get("mapname"), is("SCMP_001"));
    assertThat(result.get("map_file_path"), is("maps/SCMP_001.zip"));
    assertThat(result.get("host"), is("Player 1"));
    assertThat(result.get("num_players"), is(2));
    assertThat(result.get("max_players"), is(4));
    assertThat(result.get("launched_at"), is(0f));
    assertThat(result.get("teams"), is(ImmutableMap.of(
      "2", Collections.singletonList("Player 1"),
      "3", Collections.singletonList("Player 2")
    )));
  }

  @Test
  public void clientGameStateInitializingIsUnknown() throws Exception {
    Map<String, Serializable> result = GameResponseTransformer.INSTANCE.transform(new GameResponse(game));

    assertThat(result.get("state"), is("unknown"));
  }

  @Test
  public void clientGameStateOpenIsOpen() throws Exception {
    game.setState(GameState.OPEN);
    Map<String, Serializable> result = GameResponseTransformer.INSTANCE.transform(new GameResponse(game));

    assertThat(result.get("state"), is("open"));
  }

  @Test
  public void clientGameStatePlayingIsPlaying() throws Exception {
    game.setState(GameState.OPEN);
    game.setState(GameState.PLAYING);
    Map<String, Serializable> result = GameResponseTransformer.INSTANCE.transform(new GameResponse(game));

    assertThat(result.get("state"), is("playing"));
  }

  @Test
  public void clientGameStateClosedIsClosed() throws Exception {
    game.setState(GameState.OPEN);
    game.setState(GameState.PLAYING);
    game.setState(GameState.CLOSED);
    Map<String, Serializable> result = GameResponseTransformer.INSTANCE.transform(new GameResponse(game));

    assertThat(result.get("state"), is("closed"));
  }
}
