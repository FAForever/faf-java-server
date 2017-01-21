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

  public RequestException(ErrorCode errorCode) {
    this(errorCode, NO_ARGS, null);
  }

  public RequestException(ErrorCode errorCode, Throwable cause) {
    this(errorCode, NO_ARGS, cause);
  }

  public RequestException(ErrorCode errorCode, Object[] args) {
    this(errorCode, args, null);
  }

  public RequestException(ErrorCode errorCode, Object[] args, Throwable cause) {
    super(MessageFormat.format(errorCode.getTitle() + ": " + errorCode.getDetail(), args), cause);
    this.errorCode = errorCode;
    this.args = args;
  }
}
