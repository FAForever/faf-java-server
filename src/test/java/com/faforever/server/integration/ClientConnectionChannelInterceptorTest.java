package com.faforever.server.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientConnectionService;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Optional;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientConnectionChannelInterceptorTest {

  @Mock
  private ClientConnectionService clientConnectionService;

  private ClientConnectionChannelInterceptor instance;

  @Before
  public void setUp() throws Exception {
    instance = new ClientConnectionChannelInterceptor(clientConnectionService);
  }

  @Test
  public void preSend() throws Exception {
    String connectionId = "junit-1";
    MessageHeaders headers = new MessageHeaders(ImmutableMap.of(
      IntegrationMessageHeaderAccessor.CORRELATION_ID, connectionId
    ));
    Message<?> message = MessageBuilder.createMessage("", headers);
    MessageChannel channel = Mockito.mock(MessageChannel.class);
    TestingAuthenticationToken authentication = new TestingAuthenticationToken("junit", "");

    ClientConnection connection = new ClientConnection(connectionId, Protocol.V2_JSON_UTF_8, InetAddress.getByName("127.0.0.1"));
    connection.setAuthentication(authentication);
    when(clientConnectionService.getClientConnection(connectionId)).thenReturn(Optional.of(connection));

    Message<?> result = instance.preSend(message, channel);

    assertThat(result, notNullValue());
    assertThat(result.getHeaders().get(CLIENT_CONNECTION), instanceOf(ClientConnection.class));

    ClientConnection resultConnection = (ClientConnection) result.getHeaders().get(CLIENT_CONNECTION);
    assertThat(resultConnection, notNullValue());
    assertThat(resultConnection.getClientConnection(), is(connection));
    assertThat(resultConnection.getAuthentication(), is(authentication));

    ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
    verify(clientConnectionService).updateLastSeen(eq(connection), captor.capture());
    assertThat(captor.getValue().toEpochMilli(), is(greaterThan(Instant.now().minusSeconds(10).toEpochMilli())));
  }

  @Test(expected = IllegalStateException.class)
  public void preSendNoSuchConnection() {
    String connectionId = "junit-1";
    MessageHeaders headers = new MessageHeaders(ImmutableMap.of(
      IntegrationMessageHeaderAccessor.CORRELATION_ID, connectionId
    ));
    Message<?> message = MessageBuilder.createMessage("", headers);
    MessageChannel channel = Mockito.mock(MessageChannel.class);

    instance.preSend(message, channel);
  }
}
