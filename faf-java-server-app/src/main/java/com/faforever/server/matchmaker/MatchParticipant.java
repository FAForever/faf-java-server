package com.faforever.server.matchmaker;

import com.faforever.server.game.Faction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(of = {"id", "name"})
class MatchParticipant {
  private int id;
  private Faction faction;
  private int team;
  private String name;
  private int startSpot;
}
