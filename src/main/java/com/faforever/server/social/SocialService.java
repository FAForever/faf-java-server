package com.faforever.server.social;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.SocialRelation;
import com.faforever.server.entity.SocialRelationStatus;
import com.faforever.server.player.PlayerOnlineEvent;
import com.faforever.server.social.SocialRelationListResponse.SocialRelation.RelationType;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SocialService {

  private final SocialRelationRepository socialRelationRepository;
  private final ClientService clientService;

  public SocialService(SocialRelationRepository socialRelationRepository, ClientService clientService) {
    this.socialRelationRepository = socialRelationRepository;
    this.clientService = clientService;
  }

  public void addFriend(Player requester, int playerId) {
    socialRelationRepository.save(new SocialRelation(requester.getId(), requester, playerId, SocialRelationStatus.FRIEND));
  }

  public void addFoe(Player requester, int foeId) {
    socialRelationRepository.save(new SocialRelation(requester.getId(), requester, foeId, SocialRelationStatus.FOE));
  }

  public void removeFriend(Player requester, int friendId) {
    socialRelationRepository.deleteByPlayerIdAndSubjectIdAndStatus(requester.getId(), friendId, SocialRelationStatus.FRIEND);
  }

  public void removeFoe(Player requester, int friendId) {
    socialRelationRepository.deleteByPlayerIdAndSubjectIdAndStatus(requester.getId(), friendId, SocialRelationStatus.FOE);
  }

  @EventListener
  public void onPlayerOnlineEvent(PlayerOnlineEvent event) {
    Player player = event.getPlayer();
    List<SocialRelation> socialRelations = player.getSocialRelations();
    if (socialRelations == null) {
      return;
    }

    clientService.sendSocialRelations(new SocialRelationListResponse(
      socialRelations.stream()
        .map(socialRelation -> new SocialRelationListResponse.SocialRelation(
          socialRelation.getSubjectId(), RelationType.valueOf(socialRelation.getStatus().toString())))
        .collect(Collectors.toList())
    ), player);
  }
}
