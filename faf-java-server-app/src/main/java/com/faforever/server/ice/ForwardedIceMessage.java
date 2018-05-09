package com.faforever.server.ice;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

/**
 * ICE message that is being forwarded.
 *
 * @see IceMessage
 */
@Data
public class ForwardedIceMessage implements ServerMessage {
  private final int senderId;
  private final Object content;
}
