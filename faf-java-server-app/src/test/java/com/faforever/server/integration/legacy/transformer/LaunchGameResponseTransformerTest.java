package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.game.Faction;
import com.faforever.server.game.LobbyMode;
import com.faforever.server.game.StartGameProcessResponse;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class LaunchGameResponseTransformerTest {

  @Test
  public void transformWithoutMapAndOptionals() throws Exception {
    Map<String, Serializable> result = LaunchGameResponseTransformer.INSTANCE.transform(new StartGameProcessResponse(
      "faf",
      4,
      null,
      LobbyMode.DEFAULT,
      Faction.UEF,
      "someName",
      2,
      1,
      5,
      Arrays.asList("/numgames", "4")
    ));

    assertThat(result.get("command"), is("game_launch"));
    assertThat(result.get("mod"), is("faf"));
    assertThat(result.get("uid"), is(4));
    assertThat(result.get("mapname"), is(nullValue()));
    assertThat(result.get("lobby_mode"), is("DEFAULT"));
    assertThat(result.get("faction"), is(Faction.UEF.getString()));
    assertThat(result.get("name"), is("someName"));
    assertThat(result.get("expectedPlayers"), is(2));
    assertThat(result.get("team"), is(1));
    assertThat(result.get("mapPosition"), is(5));
    assertThat(result.get("args"), is(new String[]{"/numgames 4"}));
  }

  @Test
  public void transformWithMap() throws Exception {
    Map<String, Serializable> result = LaunchGameResponseTransformer.INSTANCE.transform(new StartGameProcessResponse(
      "faf",
      4,
      "scmp01",
      LobbyMode.DEFAULT,
      null,
      "someName",
      null,
      0,
      null,
      Arrays.asList("/numgames", "4")
    ));

    assertThat(result.get("command"), is("game_launch"));
    assertThat(result.get("mod"), is("faf"));
    assertThat(result.get("uid"), is(4));
    assertThat(result.get("mapname"), is("scmp01"));
    assertThat(result.get("lobby_mode"), is("DEFAULT"));
    assertThat(result.get("faction"), nullValue());
    assertThat(result.get("name"), is("someName"));
    assertThat(result.get("expectedPlayers"), nullValue());
    assertThat(result.get("team"), is(0));
    assertThat(result.get("mapPosition"), nullValue());
    assertThat(result.get("args"), is(new String[]{"/numgames 4"}));
  }

  @Test(expected = IllegalArgumentException.class)
  public void transformOnlyAllowsTwoArgs() throws Exception {
    LaunchGameResponseTransformer.INSTANCE.transform(new StartGameProcessResponse(
      "faf",
      4,
      null,
      LobbyMode.DEFAULT,
      null,
      "someName",
      null,
      0,
      null,
      Arrays.asList("/numgames", "4", "/mean", "1500", "/deviation", "500")
    ));
  }
}
