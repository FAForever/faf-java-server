package com.faforever.server.game;

import com.faforever.server.entity.MapVersion;
import com.faforever.server.response.ServerResponse;
import lombok.Data;

@Data
public class HostGameResponse implements ServerResponse {
  private final String mapFilename;
}
