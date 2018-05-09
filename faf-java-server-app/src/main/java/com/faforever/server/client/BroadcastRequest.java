package com.faforever.server.client;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A request to broadcast a message to all connected clients.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastRequest implements ClientMessage {
  private String message;
}
