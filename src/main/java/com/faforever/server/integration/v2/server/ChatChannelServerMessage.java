package com.faforever.server.integration.v2.server;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Message sent from the server to the client containing a list of chat channels to be joined.
 */
@Getter
@Setter
class ChatChannelServerMessage extends V2ServerMessage {
  /** A list of chat channels to be joined by the client. */
  Set<String> channels;
}
