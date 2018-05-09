package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;

/**
 * Message sent from the client to the server informing it about a changed AI option.
 */
@V2ClientNotification
class ListIceServersClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "listIceServers";
}
