package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.IntStream;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ClearSlotRequest implements ClientMessage {
  private static final int CACHE_HIGH = 17;
  private static final int CACHE_LOW = 0;
  private static final ClearSlotRequest[] cache;

  static {
    cache = new ClearSlotRequest[17];
    IntStream.range(CACHE_LOW, CACHE_HIGH).forEach(slotId -> cache[slotId] = new ClearSlotRequest(slotId));
  }

  private final int slotId;

  public static ClearSlotRequest valueOf(int slotId) {
    if (slotId < 17) {
      return cache[slotId];
    }
    return new ClearSlotRequest(slotId);
  }
}
