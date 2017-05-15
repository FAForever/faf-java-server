package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ArmyOutcomeReport implements ClientMessage {
  private static final int MAX_CACHED_VALUES = 16 * Outcome.values().length;
  private final int armyId;
  private final Outcome outcome;

  private static final Map<String, ArmyOutcomeReport> cache;

  static {
    cache = new HashMap<>();
  }

  public static ArmyOutcomeReport valueOf(int armyId, Outcome outcome) {
    // Prevent memory leak by someone flooding the cache
    if (cache.size() > MAX_CACHED_VALUES) {
      cache.entrySet().iterator().remove();
    }
    return cache.computeIfAbsent(armyId + outcome.toString(), s -> new ArmyOutcomeReport(armyId, outcome));
  }
}
