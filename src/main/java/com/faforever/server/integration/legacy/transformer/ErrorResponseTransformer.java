package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.ErrorResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum ErrorResponseTransformer implements GenericTransformer<ErrorResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(ErrorResponse source) {
    ErrorCode errorCode = source.getErrorCode();
    return ImmutableMap.of(
      "command", "notice",
      "style", "error",
      "text", errorCode.getTitle(),
      "code", errorCode.getCode()
    );
  }
}
