package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;
import lombok.Getter;
import lombok.ToString;

/**
 * @deprecated there is no need to send a session ID to the client as they make no sense in stateless protocols like the
 * legacy protocol.
 */
@Getter
@ToString
@Deprecated
public enum SessionResponse implements ServerMessage {

  INSTANCE;

  private final int sessionId = 1;
}
