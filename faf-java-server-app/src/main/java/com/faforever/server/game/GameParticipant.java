package com.faforever.server.game;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = "id")
public class GameParticipant {
  private int id;
  private Faction faction;
  private int team;
  private String name;
  private int startSpot;
}
