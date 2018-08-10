package com.faforever.server.social;

import com.faforever.server.player.SocialRelation;
import com.faforever.server.player.SocialRelationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public interface SocialRelationRepository extends JpaRepository<SocialRelation, Integer> {
  void deleteByPlayerIdAndSubjectIdAndStatus(int userId, int subjectId, SocialRelationStatus status);
}
