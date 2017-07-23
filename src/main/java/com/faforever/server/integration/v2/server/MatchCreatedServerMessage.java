package com.faforever.server.integration.v2.server;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Message sent from the server to the client containing an info message to be displayed to the user.
 */
@Getter
@Setter
class MatchCreatedServerMessage extends V2ServerMessage {
  /** ID of the original request. */
  UUID requestId;
  /** ID of the game that has been created. */
  int gameId;
}
