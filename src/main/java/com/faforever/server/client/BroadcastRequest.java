package com.faforever.server.client;

import com.faforever.server.request.ClientMessage;
import lombok.Data;

/**
 * A request to broadcast a message to all connected clients.
 */
@Data
public class BroadcastRequest implements ClientMessage {
  private final String message;
}
