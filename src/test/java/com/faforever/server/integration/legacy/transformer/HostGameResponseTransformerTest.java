package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.game.HostGameResponse;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HostGameResponseTransformerTest {
  @Test
  public void transform() throws Exception {
    HostGameResponse response = new HostGameResponse("SCMP_001");

    Map<String, Serializable> result = HostGameResponseTransformer.INSTANCE.transform(response);

    assertThat(result.get("command"), is("HostGame"));
    assertThat(result.get("target"), is("game"));
    assertThat(result.get("args"), is(new String[]{"SCMP_001"}));
  }
}
