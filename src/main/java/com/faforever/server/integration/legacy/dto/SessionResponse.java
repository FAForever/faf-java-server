package com.faforever.server.integration.legacy.dto;

import com.faforever.server.response.ServerResponse;
import lombok.Getter;

/**
 * @deprecated session IDs are deprecated even in the legacy protocol, but clients may still read it.
 */
@Deprecated
@Getter
public enum SessionResponse implements ServerResponse {

  INSTANCE;

  private final int sessionId = 1;
}
