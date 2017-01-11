package com.faforever.server.entity;

import com.faforever.server.game.GameState;
import com.faforever.server.statistics.ArmyStatistics;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "game_stats")
@EqualsAndHashCode
@Setter
@Getter
@ToString(of = {"id", "title"}, includeFieldNames = false)
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
  private String title;

  @Column(name = "validity")
  @Enumerated(EnumType.ORDINAL)
  private Rankiness rankiness;

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

  @Transient
  private final AtomicInteger desyncCount;

  @Transient
  private int maxPlayers;

  @Transient
  private List<String> simMods;

  /**
   * Maps player IDs to army scores reported by this player.
   */
  @Transient
  private final Map<Integer, List<ArmyScore>> reportedArmyScores;

  /**
   * Maps player IDs to army outcomes reported by this player.
   */
  @Transient
  private final Map<Integer, List<ArmyOutcome>> reportedArmyOutcomes;

  @Transient
  private final List<ArmyStatistics> armyStatistics;

  @Transient
  private boolean ratingEnforced;

  public Game(int id) {
    this();
    this.id = id;
  }

  public Game() {
    playerOptions = new ConcurrentHashMap<>();
    options = new ConcurrentHashMap<>();
    aiOptions = new ConcurrentHashMap<>();
    reportedArmyScores = new ConcurrentHashMap<>();
    reportedArmyOutcomes = new ConcurrentHashMap<>();
    armyStatistics = Collections.synchronizedList(new ArrayList<>());
    playerStats = Collections.synchronizedList(new ArrayList<>());
    simMods = Collections.synchronizedList(new ArrayList<>());
    desyncCount = new AtomicInteger();
    rankiness = Rankiness.RANKED;
  }

  public void replaceArmyStatistics(List<ArmyStatistics> newList) {
    synchronized (armyStatistics) {
      armyStatistics.clear();
      armyStatistics.addAll(newList);
    }
  }

  /**
   * Returns an unmodifiable list of army statistics.
   */
  public List<ArmyStatistics> getArmyStatistics() {
    return Collections.unmodifiableList(armyStatistics);
  }
}
