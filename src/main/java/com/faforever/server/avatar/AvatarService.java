package com.faforever.server.avatar;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Avatar;
import com.faforever.server.entity.Player;
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

  public void selectAvatar(Player player, String avatarUrl) {
    avatarAssociationRepository.selectAvatar(player.getId(), avatarUrl);
  }

  /**
   * Sends a list of available avatars to the specified player.
   */
  public void sendAvatarList(Player player) {
    List<Avatar> avatars = player.getAvailableAvatars().stream()
      .map(avatarAssociation -> avatarAssociation.getAvatar())
      .collect(Collectors.toList());

    clientService.sendAvatarList(avatars, player);
  }
}
