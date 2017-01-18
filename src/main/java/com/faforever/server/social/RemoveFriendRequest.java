package com.faforever.server.social;

import com.faforever.server.request.ClientMessage;
import lombok.Data;

/**
 * @deprecated removing friends should directly be requested from the API, not from the server.
 */
@Deprecated
@Data
public class RemoveFriendRequest implements ClientMessage {
  private final int playerId;
}
