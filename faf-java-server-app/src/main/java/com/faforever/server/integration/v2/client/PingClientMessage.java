package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;

/**
 * Message sent from the client to the server to let the server know that it still connected. This is needed since
 * without it, TCP timeouts are way too long (about 15min) and can't be reduced by the application.
 */
@V2ClientNotification
class PingClientMessage extends V2ClientMessage {
  public static final String TYPE_NAME = "ping";
}
