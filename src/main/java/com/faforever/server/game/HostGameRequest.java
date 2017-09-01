package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.Data;

@Data
public class HostGameRequest implements ClientMessage {

  private final String mapName;
  private final String title;
  private final String mod;
  private final GameAccess access;
  private final Integer version;
  private final String password;
  private final GameVisibility visibility;
  private final Integer minRating;
  private final Integer maxRating;
}
