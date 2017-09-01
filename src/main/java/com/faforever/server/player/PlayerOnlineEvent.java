package com.faforever.server.player;

import com.faforever.server.entity.Player;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

@Data
public class PlayerOnlineEvent extends ApplicationEvent {
  private final Player player;

  public PlayerOnlineEvent(Object source, Player player) {
    super(source);
    this.player = player;
  }
}
