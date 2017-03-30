package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;

/**
 * Sent by the game whenever a player got killed in order to enforce rating, even though the minimum game time has not
 * yet been reached.
 */
public class EnforceRatingRequest implements ClientMessage {
}
