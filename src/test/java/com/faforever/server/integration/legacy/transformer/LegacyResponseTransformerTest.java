package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.ConnectToPlayerResponse;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class LegacyResponseTransformerTest {
  private LegacyResponseTransformer instance;

  @Before
  public void setUp() throws Exception {
    instance = new LegacyResponseTransformer();
  }

  @Test
  public void transform() throws Exception {
    Map<String, Serializable> result = instance.transform(new ConnectToPlayerResponse("JUnit", 1));
    assertThat(result, is(notNullValue()));
  }
}
