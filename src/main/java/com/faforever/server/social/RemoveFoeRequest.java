package com.faforever.server.social;

import com.faforever.server.request.ClientMessage;
import lombok.Data;

/**
 * @deprecated removing foes should directly be requested from the API, not from the server.
 */
@Deprecated
@Data
public class RemoveFoeRequest implements ClientMessage {
  private final int playerId;
}
