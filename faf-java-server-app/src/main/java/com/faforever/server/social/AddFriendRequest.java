package com.faforever.server.social;

import com.faforever.server.common.ClientMessage;
import lombok.Value;

/**
 * Tells the server to add the specified player to the sending player's friend list.
 */
@Value
public class AddFriendRequest implements ClientMessage {
  int playerId;
}
