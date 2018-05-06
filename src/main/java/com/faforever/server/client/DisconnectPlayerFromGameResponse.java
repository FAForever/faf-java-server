package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

/**
 * Message sent from the server to the client, telling it that it should close the connection to the specified peer.
 */
@Data
public class DisconnectPlayerFromGameResponse implements ServerMessage {
  private final int playerId;
}
