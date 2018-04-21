package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;

/**
 * Sent by the game whenever the simulation ended.
 */
public enum GameEndedReport implements ClientMessage {
  INSTANCE
}
