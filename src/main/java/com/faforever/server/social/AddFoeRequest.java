package com.faforever.server.social;

import com.faforever.server.common.ClientMessage;
import lombok.Value;

/**
 * Tells the server to add the specified player to the sending player's foe list.
 */
@Value
public class AddFoeRequest implements ClientMessage {
  int playerId;
}
