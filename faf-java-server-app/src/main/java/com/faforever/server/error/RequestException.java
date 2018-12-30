package com.faforever.server.error;

import lombok.Getter;

import java.text.MessageFormat;
import java.util.UUID;

/**
 * Exceptions of this type are converted to messages and sent back to the client.
 */
@Getter
public class RequestException extends RuntimeException {

  private static final Object[] NO_ARGS = new Object[0];
  private final ErrorCode errorCode;
  private final Object[] args;
  private final UUID requestId;

  public RequestException(Throwable cause, ErrorCode errorCode) {
    this(null, cause, errorCode, NO_ARGS);
  }

  public RequestException(Throwable cause, ErrorCode errorCode, Object... args) {
    this(null, cause, errorCode, args);
  }

  public RequestException(ErrorCode errorCode, Object... args) {
    this(null, null, errorCode, args);
  }

  public RequestException(UUID requestId, ErrorCode errorCode, Object... args) {
    this(requestId, null, errorCode, args);
  }

  private RequestException(UUID requestId, Throwable cause, ErrorCode errorCode, Object... args) {
    super(MessageFormat.format(errorCode.getTitle() + ": " + errorCode.getDetail(), args), cause);
    this.requestId = requestId;
    this.errorCode = errorCode;
    this.args = args;
  }
}
