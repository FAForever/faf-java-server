package com.faforever.server.client;

import com.faforever.server.common.ClientMessage;

/**
 * @deprecated deprecated, even in the legacy protocol. But clients may still rely on it.
 */
@Deprecated
public final class SessionRequest implements ClientMessage {
  public static final SessionRequest INSTANCE = new SessionRequest();

  private SessionRequest() {
    // Singleton instance
  }
}
