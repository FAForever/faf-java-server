package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.GameResponses;
import com.faforever.server.entity.GameState;
import com.faforever.server.game.GameResponse;
import com.faforever.server.game.GameResponse.FeaturedModFileVersion;
import com.faforever.server.game.GameResponse.SimMod;
import com.faforever.server.game.GameVisibility;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class GameResponsesTransformerTest {

  @Test
  @SuppressWarnings("unchecked")
  public void transform() throws Exception {
    Map<String, Serializable> result = GameResponsesTransformer.INSTANCE.transform(createSingleResponses(GameState.OPEN));
    assertThat(result.get("command"), is("game_info"));

    List<Map<String, Serializable>> games = (List<Map<String, Serializable>>) result.get("games");
    assertThat(games.size(), is(1));

    Map<String, Serializable> game = games.get(0);

    assertThat(game.get("visibility"), is(GameVisibility.PUBLIC.getString()));
    assertThat(game.get("password_protected"), is(false));
    assertThat(game.get("uid"), is(123));
    assertThat(game.get("title"), is("Test"));
    assertThat(game.get("state"), is("open"));
    assertThat(game.get("featured_mod"), is("faf"));
    assertThat(game.get("featured_mod_versions"), is(ImmutableMap.of((short) 1, 1111, (short) 2, 2222)));
    assertThat(game.get("sim_mods"), is(ImmutableMap.of("1-1-1-1", "Mod #1", "2-2-2-2", "Mod #2")));
    assertThat(game.get("mapname"), is("SCMP_001"));
    assertThat(game.get("map_file_path"), is("maps/SCMP_001.zip"));
    assertThat(game.get("host"), is("Player 1"));
    assertThat(game.get("num_players"), is(4));
    assertThat(game.get("max_players"), is(6));
    assertThat((double) game.get("launched_at"), is(Matchers.lessThan(Instant.now().plusSeconds(1).toEpochMilli() / 1000d)));
    assertThat((double) game.get("launched_at"), is(Matchers.greaterThan(Instant.now().minusSeconds(10).toEpochMilli() / 1000d)));
    assertThat(game.get("min_rating"), is(1500));
    assertThat(game.get("max_rating"), is(nullValue()));
    assertThat(game.get("teams"), is(ImmutableMap.of(
      "2", Arrays.asList("Player 1", "Player 2"),
      "3", Arrays.asList("Player 3", "Player 4")
    )));
  }

  @NotNull
  private GameResponses createSingleResponses(GameState state) {
    return new GameResponses(Collections.singletonList(gameResponse(state)));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void clientGameStateInitializingIsUnknown() throws Exception {
    Map<String, Serializable> result = GameResponsesTransformer.INSTANCE.transform(createSingleResponses(GameState.INITIALIZING));

    List<Map<String, Object>> games = (List<Map<String, Object>>) result.get("games");
    assertThat(games.get(0).get("state"), is("unknown"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void clientGameStateOpenIsOpen() throws Exception {
    Map<String, Serializable> result = GameResponsesTransformer.INSTANCE.transform(createSingleResponses(GameState.OPEN));

    List<Map<String, Object>> games = (List<Map<String, Object>>) result.get("games");
    assertThat(games.get(0).get("state"), is("open"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void clientGameStatePlayingIsPlaying() throws Exception {
    Map<String, Serializable> result = GameResponsesTransformer.INSTANCE.transform(createSingleResponses(GameState.PLAYING));

    List<Map<String, Object>> games = (List<Map<String, Object>>) result.get("games");
    assertThat(games.get(0).get("state"), is("playing"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void clientGameStateClosedIsClosed() throws Exception {
    Map<String, Serializable> result = GameResponsesTransformer.INSTANCE.transform(createSingleResponses(GameState.CLOSED));

    List<Map<String, Object>> games = (List<Map<String, Object>>) result.get("games");
    assertThat(games.get(0).get("state"), is("closed"));
  }

  private GameResponse gameResponse(GameState state) {
    return new GameResponse(
      123,
      "Test",
      GameVisibility.PUBLIC,
      false,
      state,
      "faf",
      Arrays.asList(
        new SimMod("1-1-1-1", "Mod #1"),
        new SimMod("2-2-2-2", "Mod #2")
      ),
      "SCMP_001",
      "Player 1",
      Arrays.asList(
        new GameResponse.Player(1, "Player 1", 2),
        new GameResponse.Player(2, "Player 2", 2),
        new GameResponse.Player(3, "Player 3", 3),
        new GameResponse.Player(4, "Player 4", 3)
      ),
      6,
      Instant.now(),
      1500,
      null,
      1234,
      Arrays.asList(
        new FeaturedModFileVersion((short) 1, 1111),
        new FeaturedModFileVersion((short) 2, 2222)
      )
    );
  }
}
