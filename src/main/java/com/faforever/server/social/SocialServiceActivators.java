package com.faforever.server.social;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.integration.ChannelNames;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

import javax.inject.Inject;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;

@MessageEndpoint
public class SocialServiceActivators {
  private final SocialService socialService;

  @Inject
  public SocialServiceActivators(SocialService socialService) {
    this.socialService = socialService;
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_ADD_FRIEND_REQUEST)
  public void addFriend(AddFriendRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    socialService.addFriend(clientConnection.getUserDetails().getPlayer(), request.getPlayerId());
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_REMOVE_FRIEND_REQUEST)
  public void removeFriend(RemoveFriendRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    socialService.removeFriend(clientConnection.getUserDetails().getPlayer(), request.getPlayerId());
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_ADD_FOE_REQUEST)
  public void addFoe(AddFoeRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    socialService.addFoe(clientConnection.getUserDetails().getPlayer(), request.getPlayerId());
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_REMOVE_FOE_REQUEST)
  public void removeFoe(RemoveFoeRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    socialService.removeFoe(clientConnection.getUserDetails().getPlayer(), request.getPlayerId());
  }
}
