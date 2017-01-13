package com.faforever.server.error;

import lombok.Getter;

/**
 * Exceptions of this type are converted to messages and sent back to the client.
 */
@Getter
public class RequestException extends RuntimeException {

  private static final Object[] NO_ARGS = new Object[0];
  private final ErrorCode errorCode;
  private final Object[] args;

  public RequestException(ErrorCode errorCode) {
    this(errorCode, NO_ARGS);
  }

  public RequestException(ErrorCode errorCode, Object[] args) {
    super(errorCode.getTitle());
    this.errorCode = errorCode;
    this.args = args;
  }
}
