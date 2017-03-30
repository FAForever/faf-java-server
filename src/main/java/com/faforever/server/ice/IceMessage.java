package com.faforever.server.ice;

import com.faforever.server.common.ClientMessage;
import lombok.Data;

/**
 * ICE message sent by the client. Its content is unknown and irrelevant since it's just forwarded to another client.
 *
 * @see ForwardedIceMessage
 */
@Data
public class IceMessage implements ClientMessage {
  private final int receiverId;
  private final Object content;
}
