package com.faforever.server.integration.v2.client;

import com.faforever.server.common.ClientMessage;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.Requests;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Transforms messages from the v2 protocol to internal client message objects.
 */
@Slf4j
@Component
public class V2ClientMessageTransformer implements GenericTransformer<String, ClientMessage> {

  private final ObjectMapper objectMapper;
  private final V2ClientMessageMapper v2ClientMessageMapper;
  private final Map<Class<?>, Method> mapperMethods;

  public V2ClientMessageTransformer(ObjectMapper objectMapper, V2ClientMessageMapper v2ClientMessageMapper) {
    this.objectMapper = objectMapper;
    this.v2ClientMessageMapper = v2ClientMessageMapper;

    mapperMethods = Stream.of(V2ClientMessageMapper.class.getDeclaredMethods())
      .filter(method -> method.getParameterCount() == 1)
      .collect(Collectors.toMap(method -> method.getParameterTypes()[0], Function.identity()));
  }

  @Override
  @SneakyThrows
  public ClientMessage transform(String source) {
    try {
      V2ClientMessageWrapper wrapper = objectMapper.readValue(source, V2ClientMessageWrapper.class);
      V2ClientMessage message = wrapper.getData();

      Requests.verify(message != null, ErrorCode.UNSUPPORTED_REQUEST, source);

      Method mappingMethod = mapperMethods.get(message.getClass());
      Requests.verify(mappingMethod != null, ErrorCode.UNSUPPORTED_REQUEST, source);

      return (ClientMessage) mappingMethod.invoke(v2ClientMessageMapper, message);
    } catch (JsonProcessingException e) {
      throw Requests.exception(e, ErrorCode.UNSUPPORTED_REQUEST, source, e.getMessage());
    }
  }
}
