package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.ConnectToPeerResponse;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConnectToPeerResponseTransformerTest {

  @Test
  public void transform() throws Exception {
    ConnectToPeerResponse response = new ConnectToPeerResponse("JUnit", 123, true);

    Map<String, Serializable> result = ConnectToPeerResponseTransformer.INSTANCE.transform(response);

    assertThat(result.get("command"), is("ConnectToPeer"));
    assertThat(result.get("target"), is("game"));
    assertThat(result.get("args"), is(new Object[]{"JUnit", 123, true}));
  }
}
