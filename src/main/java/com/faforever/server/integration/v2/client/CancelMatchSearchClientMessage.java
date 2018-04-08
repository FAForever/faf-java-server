package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to inform the server to stop searching for a match.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
class CancelMatchSearchClientMessage extends V2ClientMessage {
  /** The name of the matchmaker pool to cancel the search for. */
  private String pool;
}
