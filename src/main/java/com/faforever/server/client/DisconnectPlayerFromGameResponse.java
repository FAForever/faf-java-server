package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

@Data
public class DisconnectPlayerFromGameResponse implements ServerMessage {
  private final int playerId;
}
