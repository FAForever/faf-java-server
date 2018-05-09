package com.faforever.server.integration.v2.client;


import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to change the current player's avatar.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientNotification
class SelectAvatarClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "selectAvatar";

  /** The ID of the new avatar. */
  private int avatarId;
}
