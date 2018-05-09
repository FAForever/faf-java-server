package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Message sent from the server to the client containing a list of chat channels to be joined.
 */
@Getter
@Setter
@V2ServerResponse
class ChatChannelServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "chatChannel";

  /** A list of chat channels to be joined by the client. */
  Set<String> channels;
}
