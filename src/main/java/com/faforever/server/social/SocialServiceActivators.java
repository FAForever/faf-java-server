package com.faforever.server.social;

import com.faforever.server.entity.Player;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.security.FafUserDetails;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.Authentication;

import javax.inject.Inject;

import static com.faforever.server.integration.MessageHeaders.USER_HEADER;

@MessageEndpoint
public class SocialServiceActivators {
  private final SocialService socialService;

  @Inject
  public SocialServiceActivators(SocialService socialService) {
    this.socialService = socialService;
  }

  @ServiceActivator(inputChannel = ChannelNames.ADD_FRIEND_REQUEST)
  public void addFriend(AddFriendRequest request, @Header(USER_HEADER) Authentication authentication) {
    socialService.addFriend(getPlayer(authentication), request.getPlayerId());
  }

  @ServiceActivator(inputChannel = ChannelNames.REMOVE_FRIEND_REQUEST)
  public void removeFriend(RemoveFriendRequest request, @Header(USER_HEADER) Authentication authentication) {
    socialService.removeFriend(getPlayer(authentication), request.getPlayerId());
  }

  @ServiceActivator(inputChannel = ChannelNames.ADD_FOE_REQUEST)
  public void addFoe(AddFoeRequest request, @Header(USER_HEADER) Authentication authentication) {
    socialService.addFoe(getPlayer(authentication), request.getPlayerId());
  }

  @ServiceActivator(inputChannel = ChannelNames.REMOVE_FOE_REQUEST)
  public void removeFoe(RemoveFoeRequest request, @Header(USER_HEADER) Authentication authentication) {
    socialService.removeFoe(getPlayer(authentication), request.getPlayerId());
  }

  private Player getPlayer(Authentication authentication) {
    return ((FafUserDetails) authentication.getPrincipal()).getPlayer();
  }
}
