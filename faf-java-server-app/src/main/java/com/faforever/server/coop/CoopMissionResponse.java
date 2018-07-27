package com.faforever.server.coop;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

/**
 * @deprecated the client should ssk the API instead
 */
@Data
@Deprecated
public class CoopMissionResponse implements ServerMessage {
  private final int id;
  private final String name;
  private final String description;
  private final String filename;
  private final CoopMissionType missionType;
}
