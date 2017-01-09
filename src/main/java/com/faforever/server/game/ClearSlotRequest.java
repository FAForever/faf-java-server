package com.faforever.server.game;

import com.faforever.server.request.ClientRequest;
import lombok.Data;

@Data
public class ClearSlotRequest implements ClientRequest {
  private final int slotId;
}
