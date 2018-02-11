package com.faforever.server.integration.v2.server;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Message sent from the server to the client containing information about the player's social relations.
 */
@Getter
@Setter
class SocialRelationServerMessage extends V2ServerMessage {
  private List<SocialRelationResponse> socialRelations;

  @Getter
  @Setter
  static class SocialRelationResponse {
    Integer playerId;
    RelationType type;

    enum RelationType {
      FRIEND, FOE
    }
  }
}
