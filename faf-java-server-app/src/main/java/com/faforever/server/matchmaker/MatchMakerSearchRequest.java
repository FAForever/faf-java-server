package com.faforever.server.matchmaker;

import com.faforever.server.common.ClientMessage;
import com.faforever.server.game.Faction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchMakerSearchRequest implements ClientMessage {
  private Faction faction;
  private String poolName;
}
