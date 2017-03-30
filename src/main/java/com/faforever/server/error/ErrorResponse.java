package com.faforever.server.error;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

@Data
public class ErrorResponse implements ServerMessage {

  private final ErrorCode errorCode;
  private final Object[] args;
}
