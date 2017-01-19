package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.SessionResponse;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SessionResponseTransformerTest {
  @Test
  public void transform() throws Exception {
    Map<String, Serializable> result = SessionResponseTransformer.INSTANCE.transform(SessionResponse.INSTANCE);

    assertThat(result.get("command"), is("session"));
    assertThat(result.get("session"), is(1));
  }
}
