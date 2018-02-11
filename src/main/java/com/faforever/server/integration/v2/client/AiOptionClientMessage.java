package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Message sent from the client to the server informing it about a changed AI option.
 */
@Getter
@AllArgsConstructor
class AiOptionClientMessage extends V2ClientMessage {
  /** The name of the AI whose option has been changed. */
  private String aiName;
  /** The AI option's key as in the game code. */
  private String key;
  /** The AI option's value. */
  private String value;
}
