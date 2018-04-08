package com.faforever.server.integration.v2.client;

import com.faforever.server.game.PlayerGameState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server informing it about the player's game state.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
class GameStateClientMessage extends V2ClientMessage {
  /** The new state of the player's game. */
  private PlayerGameState state;
}
