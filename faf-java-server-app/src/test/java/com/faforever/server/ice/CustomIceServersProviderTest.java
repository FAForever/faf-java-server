package com.faforever.server.ice;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.config.ServerProperties.Ice.Server;
import com.faforever.server.player.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CustomIceServersProviderTest {
  private CustomIceServersProvider instance;

  private ServerProperties properties;

  @Before
  public void setUp() throws Exception {
    properties = new ServerProperties();
    instance = new CustomIceServersProvider(properties);
    instance.setTimeProvider(() -> Instant.ofEpochSecond(0));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getIceServerList() {
    properties.getIce().setServers(Arrays.asList(
      new Server().setUrl("stun:localhost:1234").setSecret("secret1"),
      new Server().setUrl("turn:localhost:2345?transport=tcp").setSecret("secret2"),
      new Server().setUrl("turn:localhost:2345?transport=udp").setSecret("secret3")
    ));

    IceServerList result = instance.getIceServerList((Player) new Player().setLogin("JUnit"));

    List<IceServer> servers = result.getServers();
    assertThat(servers, hasSize(3));
    assertThat(servers.get(0).getUrl(), is(URI.create("stun:localhost:1234")));
    assertThat(servers.get(0).getCredential(), is("C3lsR1iV8r1ywUPNTWYChd+S8Iw="));
    assertThat(servers.get(0).getCredentialType(), is("token"));
    assertThat(servers.get(0).getUsername(), endsWith(":JUnit"));

    assertThat(servers.get(1).getUrl(), is(URI.create("turn:localhost:2345?transport=tcp")));
    assertThat(servers.get(1).getCredential(), is("noPPAyGebv4KrdnFRvA5zSoh+zM="));
    assertThat(servers.get(1).getCredentialType(), is("token"));
    assertThat(servers.get(1).getUsername(), endsWith(":JUnit"));

    assertThat(servers.get(2).getUrl(), is(URI.create("turn:localhost:2345?transport=udp")));
    assertThat(servers.get(2).getCredential(), is("A8+TlRGRqeZKOeD93VNJnzvlUfM="));
    assertThat(servers.get(2).getCredentialType(), is("token"));
    assertThat(servers.get(2).getUsername(), endsWith(":JUnit"));
  }
}
