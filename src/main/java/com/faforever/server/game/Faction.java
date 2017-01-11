package com.faforever.server.game;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Faction {
  // Order is crucial, and because LUA indices are 1-based, the first value here is "DUMMY".
  DUMMY, AEON, CYBRAN, UEF, SERAPHIM, NOMAD;

  @JsonValue
  public int toValue() {
    return ordinal();
  }
}
