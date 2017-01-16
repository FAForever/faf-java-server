package com.faforever.server.game;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum Faction {
  // Order is crucial, and because LUA indices are 1-based, the first value here is "DUMMY".
  DUMMY(null), AEON("aeon"), CYBRAN("cybran"), UEF("uef"), SERAPHIM("seraphim"), NOMAD("nomad");

  private static final Map<String, Faction> fromString;

  static {
    fromString = new HashMap<>();
    for (Faction faction : values()) {
      fromString.put(faction.string, faction);
    }
  }

  private final String string;

  Faction(String string) {
    this.string = string;
  }

  /**
   * Returns the faction value used as in "Forged Alliance Forever".
   */
  @JsonValue
  public int toFaValue() {
    return ordinal();
  }

  public static Faction fromString(String string) {
    return fromString.get(string);
  }
}
