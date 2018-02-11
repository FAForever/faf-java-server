package com.faforever.server.mod;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

/**
 * Sends a "featured mod" to the client.
 */
@Data
public class FeaturedModResponse implements ServerMessage {

  private final String technicalName;
  private final String displayName;
  private final String description;
  private final int displayOrder;

}
