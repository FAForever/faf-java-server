package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Message sent from the server to the client informing it that a previously requested match has been created.
 */
@Getter
@Setter
@V2ServerResponse
public class MatchCreatedServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "matchCreated";

  /** ID of the original request. */
  UUID requestId;
  /** ID of the game that has been created. */
  int gameId;
}
