package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;
import com.faforever.server.game.PlayerGameState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Message sent from the client to the server informing it about the player's game state.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientNotification
class GameStateClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "gameState";

  /** The new state of the player's game. */
  @NotNull
  private PlayerGameState state;
}
