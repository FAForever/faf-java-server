package com.faforever.server.ice;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ICE message sent by the client. Its content is unknown and irrelevant since it's just forwarded to another client.
 *
 * @see ForwardedIceMessage
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IceMessage implements ClientMessage {
  private int receiverId;
  private Object content;
}
