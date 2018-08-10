package com.faforever.server.player;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlayerOnlineEvent extends ApplicationEvent {
  private final Player player;

  public PlayerOnlineEvent(Object source, Player player) {
    super(source);
    this.player = player;
  }
}
