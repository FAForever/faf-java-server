package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Message sent from the client to the server informing it about a chat message that has been sent by a player in the
 * game lobby.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientNotification
class GameChatMessageClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "gameChatMessage";

  /** The chat message that has been sent. */
  @NotNull
  private String message;
}
