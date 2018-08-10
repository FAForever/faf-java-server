package com.faforever.server.integration;

import com.faforever.server.avatar.AvatarService;
import com.faforever.server.avatar.ListAvatarsRequest;
import com.faforever.server.avatar.SelectAvatarRequest;
import com.faforever.server.player.Player;
import com.faforever.server.security.FafUserDetails;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.Authentication;

import static com.faforever.server.integration.MessageHeaders.USER_HEADER;

@MessageEndpoint
public class AvatarServiceActivator {
  private final AvatarService avatarService;

  public AvatarServiceActivator(AvatarService avatarService) {
    this.avatarService = avatarService;
  }

  @ServiceActivator(inputChannel = ChannelNames.SELECT_AVATAR)
  public void selectAvatar(SelectAvatarRequest request, @Header(USER_HEADER) Authentication authentication) {
    avatarService.selectAvatar(getPlayer(authentication), request.getAvatarUrl(), request.getAvatarId());
  }

  @ServiceActivator(inputChannel = ChannelNames.LIST_AVATAR)
  public void listAvatars(ListAvatarsRequest request, @Header(USER_HEADER) Authentication authentication) {
    avatarService.sendAvatarList(getPlayer(authentication));
  }

  private Player getPlayer(Authentication authentication) {
    return ((FafUserDetails) authentication.getPrincipal()).getPlayer();
  }
}
