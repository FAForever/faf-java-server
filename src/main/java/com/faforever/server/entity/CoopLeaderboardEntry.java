package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Time;

@Entity
@Table(name = "coop_leaderboard")
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
  private long gameId;

  @Column(name = "secondary")
  private boolean secondary;

  @Column(name = "time")
  private Time time;

  @Column(name = "player_count")
  private int playerCount;
}
