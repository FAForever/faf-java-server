package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.game.StartGameProcessResponse;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LaunchGameResponseTransformerTest {

  @Test
  public void transform() throws Exception {
    Map<String, Serializable> result = LaunchGameResponseTransformer.INSTANCE.transform(new StartGameProcessResponse(
      "faf",
      4,
      Arrays.asList("/numgames", "4")
    ));

    assertThat(result.get("command"), is("game_launch"));
    assertThat(result.get("mod"), is("faf"));
    assertThat(result.get("uid"), is(4));
    assertThat(result.get("args"), is(new String[]{"/numgames 4"}));
  }

  @Test(expected = IllegalArgumentException.class)
  public void transformOnlyAllowsTwoArgs() throws Exception {
    LaunchGameResponseTransformer.INSTANCE.transform(new StartGameProcessResponse(
      "faf",
      4,
      Arrays.asList("/numgames", "4", "/mean", "1500", "/deviation", "500")
    ));
  }
}
