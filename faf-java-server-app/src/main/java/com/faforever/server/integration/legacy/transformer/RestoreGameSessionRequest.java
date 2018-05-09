package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestoreGameSessionRequest implements ClientMessage {
  private int gameId;
}
