package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.IceServersResponse;
import com.faforever.server.ice.IceServer;
import com.faforever.server.ice.IceServerList;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class ServersResponseTransformerTest {

  @Test
  @SuppressWarnings("unchecked")
  public void transform() throws Exception {
    Instant now = Instant.now();
    List<IceServerList> iceServersLists = Arrays.asList(
      new IceServerList(60, now, Arrays.asList(
        new IceServer(URI.create("http://localhost:1234"), "junit1", "test1"),
        new IceServer(URI.create("http://localhost:2345"), "junit2", "test2")
      ))
    );

    Map<String, Serializable> result = IceServersResponseTransformer.INSTANCE.transform(new IceServersResponse(iceServersLists));

    assertThat(result.get("command"), is("ice_servers"));
    assertThat(result.get("date_created"), is(DateTimeFormatter.ISO_INSTANT.format(now)));
    assertThat(result.get("ttl"), is(60));

    List<Map<String, Object>> iceServers = (List<Map<String, Object>>) result.get("ice_servers");
    assertThat(iceServers, hasSize(2));
    assertThat(iceServers.get(0).get("url"), is("http://localhost:1234"));
    assertThat(iceServers.get(0).get("username"), is("junit1"));
    assertThat(iceServers.get(0).get("credential"), is("test1"));
    assertThat(iceServers.get(1).get("url"), is("http://localhost:2345"));
    assertThat(iceServers.get(1).get("username"), is("junit2"));
    assertThat(iceServers.get(1).get("credential"), is("test2"));
  }
}
