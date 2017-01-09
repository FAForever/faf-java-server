package com.faforever.server.game;

import com.faforever.server.request.ClientRequest;
import lombok.Data;

@Data
public class PlayerOptionRequest implements ClientRequest {
  private final int playerId;
  private final String key;
  private final Object value;
}
