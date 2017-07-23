package com.faforever.server.matchmaker;

import com.faforever.server.game.Faction;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(of = {"id", "name"})
class MatchParticipant {
  private int id;
  private Faction faction;
  private int slot;
  private int team;
  private String name;
  private int startSpot;
}
