package com.faforever.server.error;

import java.util.UUID;

public final class Requests {
  private Requests() {
    // Private
  }

  // TODO remove this, always require a requestId (generate one for messages sent by legacy protocol)
  public static void verify(boolean expression, ErrorCode errorCode, Object... args) {
    if (!expression) {
      throw exception(errorCode, args);
    }
  }

  public static RequestException exception(ErrorCode errorCode, Object... args) {
    return new RequestException(errorCode, args);
  }

  public static RequestException exception(Throwable cause, ErrorCode errorCode, Object... args) {
    return new RequestException(cause, errorCode, args);
  }

  public static void verify(boolean expression, UUID requestId, ErrorCode errorCode, Object... args) {
    if (!expression) {
      throw new RequestException(requestId, errorCode, args);
    }
  }
}
