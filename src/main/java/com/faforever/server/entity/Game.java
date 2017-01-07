package com.faforever.server.entity;

import com.faforever.server.game.GameState;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "game_stats")
@EqualsAndHashCode
@Setter
public class Game {

  private int id;
  private Timestamp startTime;
  private VictoryCondition victoryCondition;
  private byte gameMod;
  private Player host;
  private MapVersion map;
  private String gameName;
  private byte validity;
  private List<GamePlayerStats> playerStats;
  private GameState gameState;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Basic
  @Column(name = "startTime")
  public Timestamp getStartTime() {
    return startTime;
  }

  @Basic
  @Column(name = "gameType")
  public VictoryCondition getVictoryCondition() {
    return victoryCondition;
  }

  @Basic
  @Column(name = "gameMod")
  public byte getGameMod() {
    return gameMod;
  }

  @ManyToOne
  @JoinColumn(name = "host")
  public Player getHost() {
    return host;
  }

  @ManyToOne
  @JoinColumn(name = "mapId")
  public MapVersion getMap() {
    return map;
  }

  @Basic
  @Column(name = "gameName")
  public String getGameName() {
    return gameName;
  }

  @Basic
  @Column(name = "validity")
  public byte getValidity() {
    return validity;
  }

  @OneToMany(mappedBy = "game")
  public List<GamePlayerStats> getPlayerStats() {
    return playerStats;
  }

  @Transient
  public GameState getGameState() {
    return gameState;
  }
}
