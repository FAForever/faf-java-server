package com.faforever.server.social;

import com.faforever.server.common.ClientMessage;
import lombok.Value;

/**
 * Tells the server to remove the specified player from the sending player's foe list.
 */
@Value
public class RemoveFoeRequest implements ClientMessage {
  int playerId;
}
