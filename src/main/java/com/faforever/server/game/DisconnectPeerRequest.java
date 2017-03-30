package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.Data;

/**
 * Requests the disconnection of a player from his/her current game.
 */
@Data
public class DisconnectPeerRequest implements ClientMessage {
  private final int playerId;
}
