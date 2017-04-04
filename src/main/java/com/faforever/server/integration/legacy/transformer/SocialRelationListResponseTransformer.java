package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.social.SocialRelationListResponse;
import com.faforever.server.social.SocialRelationListResponse.SocialRelation;
import com.faforever.server.social.SocialRelationListResponse.SocialRelation.RelationType;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum SocialRelationListResponseTransformer implements GenericTransformer<SocialRelationListResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(SocialRelationListResponse source) {
    return ImmutableMap.of(
      "command", "social",
      "friends", extractPlayerIds(source.getSocialRelations(), RelationType.FRIEND),
      "foes", extractPlayerIds(source.getSocialRelations(), RelationType.FOE)
    );
  }

  private ArrayList<Integer> extractPlayerIds(List<SocialRelation> relations, RelationType type) {
    return relations.stream()
      .filter(socialRelation -> socialRelation.getType() == type)
      .map(SocialRelation::getPlayerId)
      .collect(Collectors.toCollection(ArrayList::new));
  }
}
