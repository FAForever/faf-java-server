package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.common.ClientMessage;
import lombok.Data;

@Data
public class RestoreGameSessionRequest implements ClientMessage {
  private final int gameId;
}
