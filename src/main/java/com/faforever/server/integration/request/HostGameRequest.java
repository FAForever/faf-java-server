package com.faforever.server.integration.request;

import com.faforever.server.game.GameAccess;
import com.faforever.server.game.GameVisibility;
import com.faforever.server.request.ClientRequest;
import lombok.Data;

@Data
public class HostGameRequest implements ClientRequest {

  private final String mapId;
  private final String title;
  private final String mod;
  private final GameAccess access;
  private final Integer version;
  private final String password;
  private final GameVisibility visibility;
}
