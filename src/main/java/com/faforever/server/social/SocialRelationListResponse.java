package com.faforever.server.social;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

import java.util.List;

@Data
public class SocialRelationListResponse implements ServerMessage {
  private final List<SocialRelation> socialRelations;

  @Data
  public static class SocialRelation {
    private final Integer playerId;
    private final RelationType type;

    public enum RelationType {
      FRIEND, FOE
    }
  }
}
