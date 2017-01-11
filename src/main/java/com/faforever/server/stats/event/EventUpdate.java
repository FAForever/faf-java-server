package com.faforever.server.stats.event;

import lombok.Data;

@Data
public class EventUpdate {
  private final EventId eventId;
  private final int updateCount;
}
