package com.faforever.server.social;

import com.faforever.server.client.ClientService;
import com.faforever.server.player.Player;
import com.faforever.server.player.PlayerOnlineEvent;
import com.faforever.server.player.SocialRelation;
import com.faforever.server.player.SocialRelationStatus;
import com.faforever.server.social.SocialRelationListResponse.SocialRelationResponse;
import com.faforever.server.social.SocialRelationListResponse.SocialRelationResponse.RelationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SocialService {

  private final SocialRelationRepository socialRelationRepository;
  private final ClientService clientService;

  public SocialService(SocialRelationRepository socialRelationRepository, ClientService clientService) {
    this.socialRelationRepository = socialRelationRepository;
    this.clientService = clientService;
  }

  @Transactional
  public void addFriend(Player requester, int friendId) {
    removeFoe(requester, friendId);

    log.debug("Adding '{}' as a friend of player '{}'", friendId, requester);
    socialRelationRepository.save(new SocialRelation(requester.getId(), requester, friendId, SocialRelationStatus.FRIEND));
  }

  @Transactional
  public void addFoe(Player requester, int foeId) {
    removeFriend(requester, foeId);

    log.debug("Adding '{}' as a foe of player '{}'", foeId, requester);
    socialRelationRepository.save(new SocialRelation(requester.getId(), requester, foeId, SocialRelationStatus.FOE));
  }

  @Transactional
  public void removeFriend(Player requester, int friendId) {
    log.debug("Removing '{}' as a friend of player '{}'", friendId, requester);
    socialRelationRepository.deleteByPlayerIdAndSubjectIdAndStatus(requester.getId(), friendId, SocialRelationStatus.FRIEND);
  }

  @Transactional
  public void removeFoe(Player requester, int foeId) {
    log.debug("Removing '{}' as a foe of player '{}'", foeId, requester);
    socialRelationRepository.deleteByPlayerIdAndSubjectIdAndStatus(requester.getId(), foeId, SocialRelationStatus.FOE);
  }

  @EventListener
  public void onPlayerOnlineEvent(PlayerOnlineEvent event) {
    Player player = event.getPlayer();
    List<SocialRelation> socialRelations = socialRelationRepository.findAllByPlayer(player);

    if (socialRelations.isEmpty()) {
      return;
    }

    clientService.sendSocialRelations(new SocialRelationListResponse(
      socialRelations.stream()
        .map(socialRelation -> new SocialRelationResponse(
          socialRelation.getSubjectId(), RelationType.valueOf(socialRelation.getStatus().toString())))
        .collect(Collectors.toList())
    ), player);
  }
}
