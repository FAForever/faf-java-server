package com.faforever.server.integration.v2.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum Faction {
  // Order is crucial
  /** Aeon. */
  AEON("aeon"),
  /** Cybran. */
  CYBRAN("cybran"),
  /** UEF. */
  UEF("uef"),
  /** Seraphim. */
  SERAPHIM("seraphim"),
  /** Nomad. */
  NOMAD("nomad");

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
   * Returns the faction value used as in the protocol.
   */
  @JsonValue
  public String toProtocolValue() {
    return string;
  }

  @JsonCreator
  public static Faction fromProtocolValue(String value) {
    return fromString.get(value);
  }
}
