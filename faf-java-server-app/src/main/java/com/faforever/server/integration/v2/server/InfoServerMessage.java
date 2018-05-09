package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client containing an info message to be displayed to the user.
 */
@Getter
@Setter
@V2ServerResponse
class InfoServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "info";

  /** The message to be displayed to the user. */
  String message;
}
