package com.faforever.server.integration.v2.client;


import com.faforever.server.annotations.V2ClientNotification;

/**
 * Message sent from the client to the server informing it about a desync in current player's game.
 */
@V2ClientNotification
class GameDesyncClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "gameDesync";
}
