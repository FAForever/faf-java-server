package com.faforever.server.integration.v2.server;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Message sent from the server to the client containing an error.
 */
@Getter
@AllArgsConstructor
class ErrorServerMessage extends V2ServerMessage {
  /** The error code. */
  int code;
  /** The english error title, formatted with the message's arguments. */
  String title;
  /** The english error text, formatted with the message's arguments. */
  String text;
  /**
   * The request ID as specified in the client's original request that caused this error. {@code null} if none was
   * specified.
   */
  String requestId;
  /**
   * The arguments to format the error title and message. Can be used if the client wishes to display a different
   * title/message, for instance localized ones.
   */
  Object[] args;
}
