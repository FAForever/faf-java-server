package com.faforever.server.client;

import com.faforever.server.request.ClientMessage;
import lombok.Data;

/**
 * Tells the client to close itself. Its purpose is questionable.
 *
 * @deprecated this is a bad solution since clients may just ignore the command. Instead, the server should
 * just disconnect the client, it is pointless to close it.
 */
@Data
@Deprecated
public class CloseClientRequest implements ClientMessage {
  private final int playerId;
}
