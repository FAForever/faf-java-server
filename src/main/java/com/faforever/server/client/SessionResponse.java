package com.faforever.server.client;

import com.faforever.server.common.ServerResponse;
import lombok.Getter;
import lombok.ToString;

/**
 * @deprecated session IDs are deprecated even in the legacy protocol, but clients may still read it.
 */
@Deprecated
@Getter
@ToString
public enum SessionResponse implements ServerResponse {

  INSTANCE;

  private final int sessionId = 1;
}
