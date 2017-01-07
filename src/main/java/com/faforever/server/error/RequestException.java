package com.faforever.server.error;

import lombok.Getter;

/**
 * Exceptions of this type are converted to messages and sent back to the client.
 */
@Getter
public class RequestException extends RuntimeException {

  private final ErrorCode errorCode;

  public RequestException(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }
}
