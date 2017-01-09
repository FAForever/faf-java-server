package com.faforever.server.client;

import com.faforever.server.response.ServerResponse;
import lombok.Data;

@Data
public class JoinGameResponse implements ServerResponse {
  private final int hostId;
}
