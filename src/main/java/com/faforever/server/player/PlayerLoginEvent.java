package com.faforever.server.player;

import com.faforever.server.entity.Player;
import lombok.Data;

@Data
public class PlayerLoginEvent {

  private final Player player;
}
