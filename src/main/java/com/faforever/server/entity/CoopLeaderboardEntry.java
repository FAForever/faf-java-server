package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Time;

@Entity
@Table(name = "coop_leaderboard")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class CoopLeaderboardEntry {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

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
