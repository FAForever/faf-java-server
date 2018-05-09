package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientRequest;
import com.faforever.server.game.StartGameProcessResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to request joining a game.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientRequest(successResponse = StartGameProcessResponse.class)
class JoinGameClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "joinGame";

  /** The ID of the game to be joined. */
  private int id;

  /** The password to use in case the game is password protected. */
  private String password;
}
