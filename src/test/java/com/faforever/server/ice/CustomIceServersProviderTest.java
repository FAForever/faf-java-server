package com.faforever.server.ice;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.config.ServerProperties.Ice.Server;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomIceServersProviderTest {
  private CustomIceServersProvider instance;

  @Mock
  private ObjectMapper objectMapper;
  private ServerProperties properties;

  @Before
  public void setUp() throws Exception {
    properties = new ServerProperties();
    properties.getJwt().setSecret("banana");

    instance = new CustomIceServersProvider(properties, objectMapper);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getIceServerList() throws Exception {
    String claim = "{\"expiresAt\": \"ff\"}";
    when(objectMapper.writeValueAsString(any())).thenReturn(claim);
    properties.getIce().setServers(Arrays.asList(
      new Server().setUrl("http://localhost:1234"),
      new Server().setUrl("http://localhost:2345")
    ));

    IceServerList result = instance.getIceServerList();

    ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass((Class) Map.class);
    verify(objectMapper, times(2)).writeValueAsString(captor.capture());
    Map<String, Object> map = captor.getValue();
    assertThat(map.get("expiresAt"), is(notNullValue()));

    List<IceServer> servers = result.getServers();
    assertThat(servers, hasSize(2));
    assertThat(servers.get(0).getUrl(), is(URI.create("http://localhost:1234")));
    assertThat(servers.get(0).getCredential(), is(notNullValue()));
    assertThat(servers.get(0).getUsername(), is(notNullValue()));

    assertThat(servers.get(1).getUrl(), is(URI.create("http://localhost:2345")));
    assertThat(servers.get(1).getCredential(), is(notNullValue()));
    assertThat(servers.get(1).getUsername(), is(notNullValue()));

    MacSigner macSigner = new MacSigner(properties.getJwt().getSecret());
    Jwt jwt = JwtHelper.decodeAndVerify(servers.get(0).getCredential(), macSigner);

    assertThat(jwt.getClaims(), is(claim));
  }
}
