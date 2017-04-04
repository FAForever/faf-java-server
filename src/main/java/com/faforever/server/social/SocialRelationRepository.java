package com.faforever.server.social;

import com.faforever.server.entity.SocialRelation;
import com.faforever.server.entity.SocialRelationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialRelationRepository extends JpaRepository<SocialRelation, Integer> {
  void deleteByPlayerIdAndSubjectIdAndStatus(int userId, int subjedId, SocialRelationStatus status);
}
