package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

@Data
public class InfoResponse implements ServerMessage {

  private final String message;
}
