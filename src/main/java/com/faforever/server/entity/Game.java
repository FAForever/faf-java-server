package com.faforever.server.entity;

import com.faforever.server.game.GameVisibility;
import com.faforever.server.statistics.ArmyStatistics;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "game_stats")
@EqualsAndHashCode
@Setter
@Getter
@ToString(of = {"id", "title"}, includeFieldNames = false)
public class Game {

  /**
   * A key-value map of gamespecific options, like {@code "PrebuiltUnits" -> "Off"}.
   */
  @Transient
  private final Map<String, Object> options;

  @Transient
  private final AtomicInteger desyncCounter;

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
  private final Map<Integer, Player> activePlayers;

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
  @JoinColumn(name = "gameMod")
  @ManyToOne
  private FeaturedMod featuredMod;

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
  private GameState state = GameState.INITIALIZING;

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

  @Transient
  private int maxPlayers;

  @Transient
  private List<String> simMods;

  @Transient
  private boolean ratingEnforced;

  @Transient
  private GameVisibility gameVisibility;

  @Transient
  private boolean mutuallyAgreedDraw;

  public Game(int id) {
    this();
    this.id = id;
  }

  public Game() {
    playerOptions = new HashMap<>();
    options = new HashMap<>();
    aiOptions = new HashMap<>();
    reportedArmyScores = new HashMap<>();
    reportedArmyOutcomes = new HashMap<>();
    armyStatistics = new ArrayList<>();
    playerStats = new ArrayList<>();
    simMods = new ArrayList<>();
    activePlayers = new HashMap<>();
    desyncCounter = new AtomicInteger();
    rankiness = Rankiness.RANKED;
    gameVisibility = GameVisibility.PUBLIC;
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

  public Game setState(GameState state) {
    GameState.verifyTransition(this.state, state);
    this.state = state;
    return this;
  }
}
