package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to request joining a game.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
class JoinGameClientMessage extends V2ClientMessage {
  /** The ID of the game to be joined. */
  private int id;
  /** The password to use in case the game is password protected. */
  private String password;
}
