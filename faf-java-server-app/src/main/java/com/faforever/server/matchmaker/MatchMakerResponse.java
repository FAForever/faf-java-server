package com.faforever.server.matchmaker;

import com.faforever.server.common.ServerMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchMakerResponse implements ServerMessage {
  private String poolName;
}
