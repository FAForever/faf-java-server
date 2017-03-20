package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.entity.GameState;
import com.faforever.server.game.GameResponse;
import com.faforever.server.game.GameVisibility;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class GameResponseTransformerTest {

  @Test
  public void transform() throws Exception {
    GameResponse source = gameResponse(GameState.OPEN);
    Map<String, Serializable> result = GameResponseTransformer.INSTANCE.transform(source);

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
    assertThat(result.get("num_players"), is(4));
    assertThat(result.get("max_players"), is(6));
    assertThat((double) result.get("launched_at"), is(Matchers.lessThan(Instant.now().plusSeconds(1).toEpochMilli() / 1000d)));
    assertThat((double) result.get("launched_at"), is(Matchers.greaterThan(Instant.now().minusSeconds(10).toEpochMilli() / 1000d)));
    assertThat(result.get("min_rating"), is(1500));
    assertThat(result.get("max_rating"), is(nullValue()));
    assertThat(result.get("teams"), is(ImmutableMap.of(
      "2", Arrays.asList("Player 1", "Player 2"),
      "3", Arrays.asList("Player 3", "Player 4")
    )));
  }

  @Test
  public void clientGameStateInitializingIsUnknown() throws Exception {
    GameResponse source = gameResponse(GameState.INITIALIZING);
    Map<String, Serializable> result = GameResponseTransformer.INSTANCE.transform(source);

    assertThat(result.get("state"), is("unknown"));
  }

  @Test
  public void clientGameStateOpenIsOpen() throws Exception {
    GameResponse source = gameResponse(GameState.OPEN);
    Map<String, Serializable> result = GameResponseTransformer.INSTANCE.transform(source);

    assertThat(result.get("state"), is("open"));
  }

  @Test
  public void clientGameStatePlayingIsPlaying() throws Exception {
    GameResponse source = gameResponse(GameState.PLAYING);
    Map<String, Serializable> result = GameResponseTransformer.INSTANCE.transform(source);

    assertThat(result.get("state"), is("playing"));
  }

  @Test
  public void clientGameStateClosedIsClosed() throws Exception {
    GameResponse source = gameResponse(GameState.CLOSED);
    Map<String, Serializable> result = GameResponseTransformer.INSTANCE.transform(source);

    assertThat(result.get("state"), is("closed"));
  }

  private GameResponse gameResponse(GameState state) {
    return new GameResponse(
      123,
      "Test",
      GameVisibility.PUBLIC,
      null,
      state,
      "faf",
      Collections.emptyList(),
      "SCMP_001",
      "Player 1",
      Arrays.asList(
        new GameResponse.Player(2, "Player 1"),
        new GameResponse.Player(2, "Player 2"),
        new GameResponse.Player(3, "Player 3"),
        new GameResponse.Player(3, "Player 4")
      ),
      6,
      Instant.now(),
      1500,
      null
    );
  }
}
