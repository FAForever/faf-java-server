package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.game.LobbyMode;
import com.faforever.server.game.StartGameProcessResponse;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class LaunchGameResponseTransformerTest {

  @Test
  public void transformWithoutMap() throws Exception {
    Map<String, Serializable> result = LaunchGameResponseTransformer.INSTANCE.transform(new StartGameProcessResponse(
      "faf",
      4,
      null,
      LobbyMode.DEFAULT,
      Optional.empty(),
      Arrays.asList("/numgames", "4")
    ));

    assertThat(result.get("command"), is("game_launch"));
    assertThat(result.get("mod"), is("faf"));
    assertThat(result.get("uid"), is(4));
    assertThat(result.get("mapname"), is(nullValue()));
    assertThat(result.get("lobby_mode"), is("DEFAULT"));
    assertThat(result.get("args"), is(new String[]{"/numgames 4"}));
  }

  @Test
  public void transformWithMap() throws Exception {
    Map<String, Serializable> result = LaunchGameResponseTransformer.INSTANCE.transform(new StartGameProcessResponse(
      "faf",
      4,
      "scmp01",
      LobbyMode.DEFAULT,
      Optional.empty(),
      Arrays.asList("/numgames", "4")
    ));

    assertThat(result.get("command"), is("game_launch"));
    assertThat(result.get("mod"), is("faf"));
    assertThat(result.get("uid"), is(4));
    assertThat(result.get("mapname"), is("scmp01"));
    assertThat(result.get("lobby_mode"), is("DEFAULT"));
    assertThat(result.get("args"), is(new String[]{"/numgames 4"}));
  }

  @Test(expected = IllegalArgumentException.class)
  public void transformOnlyAllowsTwoArgs() throws Exception {
    LaunchGameResponseTransformer.INSTANCE.transform(new StartGameProcessResponse(
      "faf",
      4,
      null,
      LobbyMode.DEFAULT,
      Optional.empty(),
      Arrays.asList("/numgames", "4", "/mean", "1500", "/deviation", "500")
    ));
  }
}
