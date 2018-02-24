package com.faforever.server.integration.v2.server;

import com.faforever.server.common.ServerMessage;
import com.faforever.server.error.ProgrammingError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Transforms messages sent from the server to the client into the v2 message format.
 */
@Component
public class V2ServerMessageTransformer implements GenericTransformer<ServerMessage, String> {

  private final ObjectMapper objectMapper;
  private final V2ServerMessageMapper v2ServerMessageMapper;
  private final Map<Class<?>, Method> mapperMethods;

  public V2ServerMessageTransformer(ObjectMapper objectMapper, V2ServerMessageMapper v2ServerMessageMapper) {
    this.objectMapper = objectMapper;
    this.v2ServerMessageMapper = v2ServerMessageMapper;

    mapperMethods = Stream.of(V2ServerMessageMapper.class.getDeclaredMethods())
      .filter(method -> method.getParameterCount() == 1)
      .collect(Collectors.toMap(method -> method.getParameterTypes()[0], Function.identity()));
  }

  @Override
  @SneakyThrows
  public String transform(ServerMessage message) {
    Class<? extends ServerMessage> messageClass = message.getClass();
    Method method = Optional.ofNullable(mapperMethods.get(messageClass))
      .orElseThrow(() -> new ProgrammingError("No message mapping method is available for '" + messageClass + "' in '" + V2ServerMessageMapper.class + "'"));

    V2ServerMessage v2ServerMessage = (V2ServerMessage) method.invoke(v2ServerMessageMapper, message);
    return objectMapper.writeValueAsString(new V2ServerMessageWrapper(v2ServerMessage));
  }
}
