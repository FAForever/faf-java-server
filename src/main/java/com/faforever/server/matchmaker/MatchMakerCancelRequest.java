package com.faforever.server.matchmaker;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Requests to cancel matchmaker search.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchMakerCancelRequest implements ClientMessage {
  private String poolName;
}
