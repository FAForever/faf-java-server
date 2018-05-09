package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Message sent from the client to the server to broadcast a text message to all connected clients.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientNotification
class BroadcastClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "broadcast";

  /** The message to be broadcasted. */
  @NotNull
  private String message;
}
