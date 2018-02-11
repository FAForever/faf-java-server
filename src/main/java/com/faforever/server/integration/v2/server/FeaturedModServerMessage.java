package com.faforever.server.integration.v2.server;

import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client containing information about an available featured mod.
 */
@Getter
@Setter
class FeaturedModServerMessage extends V2ServerMessage {
  /** The technical name of the mod, e.g. "faf". */
  String technicalName;
  /** The name to be displayed, e.g. "Forged Alliance Forever. */
  String displayName;
  /** A description that explains the featured mod. */
  String description;
  /** The order in which the featured mod should be displayed in the client. */
  int displayOrder;
}
