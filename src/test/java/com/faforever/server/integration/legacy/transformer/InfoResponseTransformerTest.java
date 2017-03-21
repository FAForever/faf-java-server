package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.InfoResponse;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InfoResponseTransformerTest {

  @Test
  public void transform() {
    InfoResponse infoResponse = new InfoResponse("This is an information message.");

    Map<String, Serializable> transformedResponse = InfoResponseTransformer.INSTANCE.transform(infoResponse);

    assertThat(transformedResponse.get("command"), is("notice"));
    assertThat(transformedResponse.get("style"), is("info"));
    assertThat(transformedResponse.get("text"), is("This is an information message."));
  }

}
