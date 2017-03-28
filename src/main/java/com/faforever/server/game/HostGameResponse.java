package com.faforever.server.game;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

@Data
public class HostGameResponse implements ServerMessage {
  private final String mapFilename;
}
