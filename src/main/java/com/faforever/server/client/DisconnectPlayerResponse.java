package com.faforever.server.client;

import com.faforever.server.common.ServerResponse;
import lombok.Data;

@Data
public class DisconnectPlayerResponse implements ServerResponse {
  private final int playerId;
}
