package com.faforever.server.integration.v2.server;


import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client informing it about an available match.
 */
@Getter
@Setter
@V2ServerResponse
class MatchAvailableServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "matchAvailable";

  /** The name of the matchmaker pool that has available matches for the receiving player. */
  private String pool;
}
