package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.DisconnectPlayerFromGameResponse;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DisconnectPeerResponseTransformerTest {
  @Test
  public void transform() {
    Map<String, Serializable> response = DisconnectPeerResponseTransformer.INSTANCE.transform(new DisconnectPlayerFromGameResponse(51));

    assertThat(response.get("command"), is("DisconnectFromPeer"));
    assertThat(response.get("peer_id"), is(51));
    assertThat(response.get("target"), is("game"));
  }
}
