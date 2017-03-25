package com.faforever.server.coop;

import com.faforever.server.common.ServerResponse;
import lombok.Data;

/**
 * @deprecated the client should ssk the API instead
 */
@Data
@Deprecated
public class CoopMissionResponse implements ServerResponse {
  private final String name;
  private final String description;
  private final String filename;
}
