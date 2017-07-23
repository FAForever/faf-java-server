package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Message sent from the client to the server to restore the current player's game session. When a client loses its
 * connection to the server, the server will remove the respective player from the game (not kick, but in the server's
 * memory). This message allows to re-add the player to the game. If this message is not sent, the player can continue
 * to play the game but the server will not accept any messages regarding that game from this client anymore.
 */
@Getter
@AllArgsConstructor
class RestoreGameSessionClientMessage extends V2ClientMessage {
  /** ID of the game the player previously participated. */
  private int gameId;
}
