package com.faforever.server.matchmaker;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

@Data
public class MatchMakerResponse implements ServerMessage {
  private final String queueName;
}
