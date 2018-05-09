package com.faforever.server.client;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Requests the disconnection of a user's client.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisconnectClientRequest implements ClientMessage {
  private int playerId;
}
