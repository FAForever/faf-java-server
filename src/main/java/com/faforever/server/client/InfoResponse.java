package com.faforever.server.client;

import com.faforever.server.common.ServerResponse;
import lombok.Data;

@Data
public class InfoResponse implements ServerResponse {

  private final String message;
}
