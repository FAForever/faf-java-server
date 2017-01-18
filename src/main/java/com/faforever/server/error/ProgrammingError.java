package com.faforever.server.error;

/**
 * Exception to be thrown whenever something happened that's not supposed to happen unless there is a programming error.
 */
public class ProgrammingError extends Error {

  public ProgrammingError(String message) {
    super(message);
  }
}
