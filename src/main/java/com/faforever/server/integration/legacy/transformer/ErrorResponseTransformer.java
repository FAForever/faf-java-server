package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.ErrorResponse;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;

public enum ErrorResponseTransformer implements GenericTransformer<ErrorResponse, Map<String, Serializable>> {
  INSTANCE;

  private static final Set<ErrorCode> AUTHENTICATION_ERRORS = Sets.newHashSet(
    ErrorCode.INVALID_LOGIN,
    ErrorCode.UID_USED_BY_ANOTHER_USER,
    ErrorCode.UID_USED_BY_MULTIPLE_USERS
  );

  @Override
  public Map<String, Serializable> transform(ErrorResponse source) {
    ErrorCode errorCode = source.getErrorCode();
    return ImmutableMap.of(
      "command", AUTHENTICATION_ERRORS.contains(errorCode) ? "authentication_failed" : "notice",
      "style", "error",
      "text", MessageFormat.format(errorCode.getTitle() + ": " + errorCode.getDetail(), source.getArgs()),
      "code", errorCode.getCode()
    );
  }
}
