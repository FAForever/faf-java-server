package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.DisconnectPlayerResponse;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DisconnectPeerResponseTransformerTest {
  @Test
  public void transform() throws Exception {
    Map<String, Serializable> response = DisconnectPeerResponseTransformer.INSTANCE.transform(new DisconnectPlayerResponse(51));

    assertThat(response.get("command"), is("DisconnectPeer"));
    assertThat(response.get("peer_id"), is(51));
  }
}
