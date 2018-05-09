package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client containing information about an available featured mod.
 */
@Getter
@Setter
@V2ServerResponse
class FeaturedModServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "featuredMod";

  /** The technical name of the mod, e.g. {@code faf}. */
  String technicalName;
  /** The name to be displayed, e.g. {@code Forged Alliance Forever}. */
  String displayName;
  /** A description that explains the featured mod. */
  String description;
  /** The order in which the featured mod should be displayed in the client. */
  int displayOrder;
}
