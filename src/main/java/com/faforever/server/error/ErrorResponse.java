package com.faforever.server.error;

import com.faforever.server.common.ServerResponse;
import lombok.Data;

@Data
public class ErrorResponse implements ServerResponse {

  private final ErrorCode errorCode;
  private final Object[] args;
}
