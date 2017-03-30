package com.faforever.server.matchmaker;

import com.faforever.server.common.ClientMessage;
import lombok.Data;


/**
 * Requests to cancel matchmaker search.
 */
@Data
public class MatchMakerCancelRequest implements ClientMessage {
  private final String queueName;
}
