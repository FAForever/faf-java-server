package com.faforever.server.social;

import com.faforever.server.entity.Player;
import com.faforever.server.entity.SocialRelation;
import com.faforever.server.entity.SocialRelationStatus;
import org.springframework.stereotype.Service;

@Service
public class SocialService {

  private final SocialRelationRepository socialRelationRepository;

  public SocialService(SocialRelationRepository socialRelationRepository) {
    this.socialRelationRepository = socialRelationRepository;
  }

  public void addFriend(Player requester, int playerId) {
    socialRelationRepository.save(new SocialRelation(requester.getId(), playerId, SocialRelationStatus.FRIEND));
  }

  public void addFoe(Player requester, int foeId) {
    socialRelationRepository.save(new SocialRelation(requester.getId(), foeId, SocialRelationStatus.FOE));
  }

  public void removeFriend(Player requester, int friendId) {
    socialRelationRepository.deleteByUserIdAndSubjectIdAndStatus(requester.getId(), friendId, SocialRelationStatus.FRIEND);
  }

  public void removeFoe(Player requester, int friendId) {
    socialRelationRepository.deleteByUserIdAndSubjectIdAndStatus(requester.getId(), friendId, SocialRelationStatus.FOE);
  }
}
