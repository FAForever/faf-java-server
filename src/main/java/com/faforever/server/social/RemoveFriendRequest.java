package com.faforever.server.social;

import com.faforever.server.common.ClientMessage;
import lombok.Value;

/**
 * Tells the server to remove the specified player from the sending player's friend list.
 */
@Value
public class RemoveFriendRequest implements ClientMessage {
  int playerId;
}
