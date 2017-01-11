package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
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
  private byte ai;

  @Column(name = "faction")
  private byte faction;

  @Column(name = "color")
  private byte color;

  @Column(name = "team")
  private byte team;

  @Column(name = "place")
  private byte place;

  @Column(name = "mean")
  private Double mean;

  @Column(name = "deviation")
  private Double deviation;

  @Column(name = "after_mean")
  private Double afterMean;

  @Column(name = "after_deviation")
  private Double afterDeviation;

  @Column(name = "score")
  private byte score;

  @Column(name = "scoreTime")
  private Timestamp scoreTime;

  @ManyToOne
  @JoinColumn(name = "gameId")
  private Game game;
}
