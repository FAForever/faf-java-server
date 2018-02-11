package com.faforever.server.integration.v2.server;


import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client informing it about an available match.
 */
@Getter
@Setter
class MatchAvailableServerMessage extends V2ServerMessage {
  String pool;
}
