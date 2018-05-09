package com.faforever.server.integration.v2.client;


import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Message sent from the client to the server, containing an ICE message to be forwarded to the specified received.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientNotification
class IceClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "iceMessage";

  /** ID of the player to send the message to. */
  private int receiverId;

  /** The ICE message content. */
  @NotNull
  private Object content;
}
