package com.faforever.server.error;

public final class Requests {
  private Requests() {
    // Private
  }

  public static void verify(boolean expression, ErrorCode errorCode, Object... args) {
    if (!expression) {
      throw new RequestException(errorCode, args);
    }
  }

  public static void fail(ErrorCode errorCode, Object... args) {
    //noinspection ConstantConditions
    verify(false, errorCode, args);
  }
}
