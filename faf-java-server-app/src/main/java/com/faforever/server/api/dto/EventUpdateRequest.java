package com.faforever.server.api.dto;

import com.faforever.server.stats.event.EventUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventUpdateRequest {

  private final String playerId;
  private final String eventId;
  private final int count;

  public static EventUpdateRequest fromInternal(EventUpdate eventUpdate) {
    return new EventUpdateRequest(
      String.valueOf(eventUpdate.getPlayerId()),
      eventUpdate.getEventId().getId(),
      eventUpdate.getUpdateCount()
    );
  }
}
