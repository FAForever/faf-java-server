package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "game_player_stats")
@Immutable
@Data
@NoArgsConstructor
public class GamePlayerStats {

  @Id
  @Column(name = "id")
  @GeneratedValue
  private long id;

  @ManyToOne
  @JoinColumn(name = "playerId")
  private Player player;

  @Column(name = "AI")
  private boolean ai;

  @Column(name = "faction")
  private int faction;

  @Column(name = "color")
  private int color;

  @Column(name = "team")
  private int team;

  @Column(name = "place")
  private int place;

  @Column(name = "mean")
  private Double mean;

  @Column(name = "deviation")
  private Double deviation;

  @Column(name = "after_mean")
  private Double afterMean;

  @Column(name = "after_deviation")
  private Double afterDeviation;

  @Column(name = "score")
  private int score;

  @Column(name = "scoreTime")
  private Timestamp scoreTime;

  @ManyToOne
  @JoinColumn(name = "gameId")
  private Game game;
}
