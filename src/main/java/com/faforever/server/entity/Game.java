package com.faforever.server.entity;

import com.faforever.server.game.GameState;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "game_stats")
@EqualsAndHashCode
@Setter
@Getter
@ToString(of = {"id", "gameName"}, includeFieldNames = false)
public class Game {

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "startTime")
  private Timestamp startTime;

  @Column(name = "gameType")
  @Enumerated(EnumType.ORDINAL)
  private VictoryCondition victoryCondition;

  /**
   * Foreign key to "featured mod", but since there's no constraint in the database yet, hope for the best.
   */
  @Column(name = "gameMod")
  private byte gameMod;

  @ManyToOne
  @JoinColumn(name = "host")
  private Player host;

  @ManyToOne
  @JoinColumn(name = "mapId")
  private MapVersion map;

  @Column(name = "gameName")
  private String gameName;

  @Column(name = "validity")
  private byte validity;

  @OneToMany(mappedBy = "game")
  private List<GamePlayerStats> playerStats;

  @Transient
  private GameState gameState;

  @Transient
  private String password;

  /**
   * Since some maps are unknown by the server (e.g. in-develop versions from map creators), the literal map name
   * is kept.
   */
  @Transient
  private String mapName;

  /**
   * Maps player IDs to key-value option maps, like {@code 1234 -> "Color" -> 1 }
   */
  @Transient
  private Map<Integer, Map<String, Object>> playerOptions;

  /**
   * Maps AI names to key-value option maps, like {@code "AI: Rufus" -> "Color" -> 1 }
   */
  @Transient
  private Map<String, Map<String, Object>> aiOptions;

  /**
   * A key-value map of gamespecific options, like {@code "PrebuiltUnits" -> "Off"}.
   */
  @Transient
  private final Map<String, Object> options;

  public Game(int id) {
    this();
    this.id = id;
  }

  public Game() {
    playerOptions = new HashMap<>();
    playerStats = new ArrayList<>();
    options = new HashMap<>();
    aiOptions = new HashMap<>();
  }
}
