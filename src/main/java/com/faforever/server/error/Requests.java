package com.faforever.server.error;

public final class Requests {
  private Requests() {
    // Private
  }

  public static void verify(boolean expression, ErrorCode errorCode) {
    if (!expression) {
      throw new RequestException(errorCode);
    }
  }
}
