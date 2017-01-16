package com.faforever.server.matchmaker;

import com.faforever.server.response.ServerResponse;
import lombok.Data;

@Data
public class MatchMakerResponse implements ServerResponse {
  private final String queueName;
}
