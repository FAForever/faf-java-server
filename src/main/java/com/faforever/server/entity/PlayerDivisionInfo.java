package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ladder_division_score")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "league", "player", "score"}, includeFieldNames = true)
public class PlayerDivisionInfo {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "season", nullable = false)
  private int season;

  @ManyToOne
  @JoinColumn(name="user_id")
  private Player player;

  @Column(name = "league", nullable = false)
  private int league;

  @Column(name = "score", nullable = false)
  private float score;

  @Column(name = "games")
  private int games;

  public boolean isInInferiorLeague(PlayerDivisionInfo comparedTo) {
    return league < comparedTo.league;
  }

  public boolean isInSuperiorLeague(PlayerDivisionInfo comparedTo) {
    return league > comparedTo.league;
  }
}
