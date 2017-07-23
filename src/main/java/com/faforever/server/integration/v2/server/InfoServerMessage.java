package com.faforever.server.integration.v2.server;

import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client containing an info message to be displayed to the user.
 */
@Getter
@Setter
class InfoServerMessage extends V2ServerMessage {
  /** The message to be displayed to the user. */
  String message;
}
