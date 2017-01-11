package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Time;

@Entity
@Table(name = "coop_leaderboard", schema = "faf_lobby", catalog = "")
@Data
@NoArgsConstructor
public class CoopLeaderboardEntry {

  @Id
  @Column(name = "id")
  @GeneratedValue
  private int id;

  @ManyToOne
  @JoinColumn(name = "mission")
  private CoopMap mission;

  @Column(name = "gameuid")
  private long gameuid;

  @Column(name = "secondary")
  private boolean secondary;

  @Column(name = "time")
  private Time time;

  @Column(name = "player_count")
  private Byte playerCount;
}
