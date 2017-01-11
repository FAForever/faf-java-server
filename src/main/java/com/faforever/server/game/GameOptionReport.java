package com.faforever.server.game;

import com.faforever.server.request.ClientMessage;
import lombok.Data;

@Data
public class GameOptionReport implements ClientMessage {
  private final String key;
  private final Object value;
}
