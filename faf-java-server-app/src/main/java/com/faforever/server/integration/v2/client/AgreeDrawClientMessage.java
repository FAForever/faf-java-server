package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;

/**
 * Message sent from the client to the server informing it that the sending player would like to agree to a draw.
 */
@V2ClientNotification
class AgreeDrawClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "agreeDraw";
}
