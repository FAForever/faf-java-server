package com.faforever.server.mod;

import com.faforever.server.response.ServerResponse;
import lombok.Data;

/**
 * Sends a "featured mod" to the client.
 *
 * @deprecated this should not be pushed to the client, instead the client should fetch it from the API.
 */
@Deprecated
@Data
public class FeaturedModResponse implements ServerResponse {

  private final String technicalName;
  private final String displayName;
  private final String description;
  private final int displayOrder;

}
