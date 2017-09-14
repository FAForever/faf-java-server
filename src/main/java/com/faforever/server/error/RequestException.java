package com.faforever.server.error;

import lombok.Getter;

import java.text.MessageFormat;

/**
 * Exceptions of this type are converted to messages and sent back to the client.
 */
@Getter
public class RequestException extends RuntimeException {

  private static final Object[] NO_ARGS = new Object[0];
  private final ErrorCode errorCode;
  private final Object[] args;

  public RequestException(Throwable cause, ErrorCode errorCode) {
    this(cause, errorCode, NO_ARGS);
  }

  public RequestException(ErrorCode errorCode, Object... args) {
    this(null, errorCode, args);
  }

  private RequestException(Throwable cause, ErrorCode errorCode, Object...args) {
    super(MessageFormat.format(
      errorCode.getTitle().replace("'", "''")
        + ": "
        + errorCode.getDetail().replace("'", "''"), args), cause);
    this.errorCode = errorCode;
    this.args = args;
  }
}
