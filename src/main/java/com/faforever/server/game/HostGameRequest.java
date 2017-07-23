package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HostGameRequest implements ClientMessage {

  private String mapName;
  private String title;
  private String mod;
  private String password;
  private GameVisibility visibility;
  private Integer minRating;
  private Integer maxRating;
}
