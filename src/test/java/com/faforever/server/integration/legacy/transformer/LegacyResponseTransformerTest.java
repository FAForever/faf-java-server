package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.ConnectToPlayerResponse;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class LegacyResponseTransformerTest {

  @Test
  public void transform() throws Exception {
    Map<String, Serializable> result = LegacyResponseTransformer.INSTANCE.transform(new ConnectToPlayerResponse("JUnit", 1));
    assertThat(result, is(notNullValue()));
  }
}
