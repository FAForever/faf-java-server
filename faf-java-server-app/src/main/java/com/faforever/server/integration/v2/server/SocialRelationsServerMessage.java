package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Message sent from the server to the client containing information about the player's social relations.
 */
@Getter
@Setter
@V2ServerResponse
class SocialRelationsServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "socialRelations";

  /** The list of social relations. */
  @NotNull
  private List<SocialRelation> socialRelations;

  /** Defines a social relation the current player has to another player has to another (uni-directional). */
  @Getter
  @Setter
  static class SocialRelation {

    /** The ID of the "other" player affected by this relation. */
    @NotNull
    int playerId;
    /** The type of the relation. */
    @NotNull
    RelationType type;

    /** The type of the social relation. */
    enum RelationType {
      /** The "current" player sees the "other" player as a friend. */
      FRIEND,
      /** The "current" player sees the "other" player as a foe. */
      FOE
    }
  }
}
