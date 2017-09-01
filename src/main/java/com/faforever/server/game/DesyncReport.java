package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;

/**
 * Sent by the client whenever a desynchronization of the game state occurred.
 */
public enum DesyncReport implements ClientMessage {

  INSTANCE
}
