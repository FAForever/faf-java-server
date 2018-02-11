package com.faforever.server.error;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

import java.util.UUID;

@Data
public class ErrorResponse implements ServerMessage {
  private final ErrorCode errorCode;
  private final UUID requestId;
  private final Object[] args;
}
