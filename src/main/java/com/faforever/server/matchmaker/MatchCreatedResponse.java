package com.faforever.server.matchmaker;

import com.faforever.server.common.ServerMessage;
import lombok.Value;

import java.util.UUID;

@Value
public class MatchCreatedResponse implements ServerMessage {
  UUID requestId;
  int gameId;
}
