package com.faforever.server.ice;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class IceServiceTest {
  private IceService instance;
  private Player player;

  @Mock
  private ClientService clientService;

  @Before
  public void setUp() throws Exception {
    player = new Player();
    List<IceServersProvider> serverProviders = Arrays.asList(
      () -> new IceServerList(60, Instant.now(), Arrays.asList(
        new IceServer(URI.create("http://localhost:1234"), "user1", "password1"),
        new IceServer(URI.create("http://localhost:2345"), "user2", "password2")
      )),
      () -> new IceServerList(60, Instant.now(), Arrays.asList(
        new IceServer(URI.create("http://localhost:3465"), "user3", "password3"),
        new IceServer(URI.create("http://localhost:4567"), "user4", "password4")
      ))
    );
    instance = new IceService(clientService, serverProviders);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void requestIceServers() throws Exception {
    instance.requestIceServers(player);

    ArgumentCaptor<List<IceServerList>> captor = ArgumentCaptor.forClass((Class) List.class);
    verify(clientService).sendIceServers(captor.capture(), eq(player));

    List<IceServerList> value = captor.getValue();

    assertThat(value, hasSize(2));
    assertThat(value.get(0).getServers().get(0).getUrl(), is(URI.create("http://localhost:1234")));
    assertThat(value.get(0).getServers().get(0).getUsername(), is("user1"));
    assertThat(value.get(0).getServers().get(0).getCredential(), is("password1"));

    assertThat(value.get(1).getServers().get(1).getUrl(), is(URI.create("http://localhost:4567")));
    assertThat(value.get(1).getServers().get(1).getUsername(), is("user4"));
    assertThat(value.get(1).getServers().get(1).getCredential(), is("password4"));
  }
}
