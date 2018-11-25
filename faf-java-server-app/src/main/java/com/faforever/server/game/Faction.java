package com.faforever.server.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum Faction {
  // Order is crucial
  AEON("aeon"), CYBRAN("cybran"), UEF("uef"), SERAPHIM("seraphim"), NOMAD("nomad");

  private static final Map<String, Faction> fromString;

  static {
    fromString = new HashMap<>();
    for (Faction faction : values()) {
      fromString.put(faction.string, faction);
    }
  }

  // TODO: This is legacy. Remove.
  @Getter
  private final String string;

  Faction(String string) {
    this.string = string;
  }

  /**
   * Returns the faction value used as in "Forged Alliance Forever".
   */
  @JsonValue
  public int toFaValue() {
    return ordinal() + 1;
  }

  @JsonCreator
  public static Faction fromFaValue(int value) {
    return Faction.values()[value - 1];
  }

  public static Faction fromString(String string) {
    return fromString.get(string);
  }
}
