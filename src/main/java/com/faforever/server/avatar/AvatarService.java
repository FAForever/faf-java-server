package com.faforever.server.avatar;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Avatar;
import com.faforever.server.entity.AvatarAssociation;
import com.faforever.server.entity.Player;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.Requests;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvatarService {
  private final AvatarAssociationRepository avatarAssociationRepository;
  private final ClientService clientService;

  public AvatarService(AvatarAssociationRepository avatarAssociationRepository, ClientService clientService) {
    this.avatarAssociationRepository = avatarAssociationRepository;
    this.clientService = clientService;
  }

  /**
   * @param avatarUrl (deprecated) only used by the legacy client, will be removed in future. Either this or {@code
   * avatarId} must be set.
   * @param avatarId Either this or {@code avatarUrl} must be set.
   */
  public void selectAvatar(Player player, String avatarUrl, Integer avatarId) {
    Requests.verify(avatarId != null || avatarUrl != null, ErrorCode.EITHER_AVATAR_ID_OR_URL);
    if (avatarUrl != null) {
      avatarAssociationRepository.selectAvatar(player.getId(), avatarUrl);
    } else {
      avatarAssociationRepository.selectAvatar(player.getId(), avatarId);
    }
  }

  /**
   * Sends a list of available avatars to the specified player.
   */
  public void sendAvatarList(Player player) {
    List<Avatar> avatars = player.getAvailableAvatars().stream()
      .map(AvatarAssociation::getAvatar)
      .collect(Collectors.toList());

    clientService.sendAvatarList(avatars, player);
  }
}
