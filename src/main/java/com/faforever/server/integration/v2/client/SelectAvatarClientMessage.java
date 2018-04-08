package com.faforever.server.integration.v2.client;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to change the current player's avatar.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
class SelectAvatarClientMessage extends V2ClientMessage {
  /** The ID of the new avatar. */
  private int avatarId;
}
