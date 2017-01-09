package com.faforever.server.game;

import com.faforever.server.request.ClientRequest;
import lombok.Data;

@Data
public class AiOptionRequest implements ClientRequest {
  private final String aiName;
  private final String key;
  private final Object value;
}
