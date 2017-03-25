package com.faforever.server.game;

import com.faforever.server.common.ServerResponse;
import lombok.Data;

@Data
public class HostGameResponse implements ServerResponse {
  private final String mapFilename;
}
