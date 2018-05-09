package com.faforever.server.stats;

import com.faforever.server.game.Faction;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Map;

@Data
public class ArmyStatistics {
  private Faction faction;
  private General general;
  private String name;
  private BrainType type;
  @JsonProperty("blueprints")
  @JsonDeserialize(using = MapOfUnitStatsDeserializer.class)
  private Map<String, UnitStats> unitStats;
  @JsonProperty("units")
  private CategoryStats categoryStats;
  private ResourceStats resources;

  public enum BrainType {
    AI("AI"), HUMAN("Human");

    private final String string;

    BrainType(String string) {
      this.string = string;
    }

    @JsonValue
    public String getString() {
      return string;
    }
  }

  @Data
  @JsonIgnoreProperties({"currentcap", "currentunits"})
  public static class General {
    private float score;
    private float mass;
    private float lastReclaimedMass;
    private float lastReclaimedEnergy;
    private float energy;
    private CountMassEnergy kills;
    private CountMassEnergy built;
    private CountMassEnergy lost;
  }

  @Data
  public static class CountMassEnergy {
    private int count;
    private float mass;
    private float energy;
  }

  @Data
  public static class CategoryStats {
    private UnitStats air;
    private UnitStats land;
    private UnitStats naval;
    private UnitStats experimental;
    private UnitStats cdr;
    private UnitStats tech1;
    private UnitStats tech2;
    private UnitStats tech3;
    private UnitStats engineer;
    private UnitStats transportation;
    private UnitStats sacu;
    private UnitStats structures;
  }

  @Data
  public static class UnitStats {
    private int built;
    private int lost;
    @JsonProperty("kills")
    private int killed;
    @JsonProperty("lowest_health")
    private int lowestHealth;
  }

  @Data
  public static class ResourceStats {
    @JsonProperty("massin")
    private TotalRate massIn;
    @JsonProperty("massout")
    private TotalRate massOut;
    @JsonProperty("energyin")
    private TotalRate energyIn;
    @JsonProperty("energyout")
    private TotalRate energyOut;
    @JsonProperty("massover")
    private float massOver;
    @JsonProperty("energyover")
    private float energyOver;
  }

  @Data
  public static class TotalRate {
    private float total;
    private float rate;
  }
}
