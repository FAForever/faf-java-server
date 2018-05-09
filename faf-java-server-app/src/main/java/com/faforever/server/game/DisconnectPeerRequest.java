package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Requests the disconnection of a player from his/her current game.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisconnectPeerRequest implements ClientMessage {
  private int playerId;
}
