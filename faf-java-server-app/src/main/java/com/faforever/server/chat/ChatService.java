package com.faforever.server.chat;

import com.faforever.server.client.ClientService;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.config.ServerProperties.Chat;
import com.faforever.server.error.ProgrammingError;
import com.faforever.server.player.PlayerOnlineEvent;
import com.faforever.server.security.GroupAssociation;
import com.faforever.server.security.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class ChatService {

  private final ServerProperties properties;
  private final ClientService clientService;

  public ChatService(ServerProperties properties, ClientService clientService) {
    this.properties = properties;
    this.clientService = clientService;
  }

  @EventListener
  public void onPlayerOnlineEvent(PlayerOnlineEvent event) {
    Chat chat = properties.getChat();

    Set<String> channels = new HashSet<>(3, 1);
    channels.addAll(chat.getDefaultChannels());

    Optional.ofNullable(event.getPlayer().getClan())
      .map(clan -> (String.format(chat.getClanChannelFormat(), clan.getTag())))
      .ifPresent(channels::add);

    User user = event.getPlayer().getUser();
    GroupAssociation groupAssociation = user.getGroupAssociation();
    if (groupAssociation != null) {
      switch (groupAssociation.getGroup()) {
        case ADMIN:
          channels.addAll(chat.getAdminChannels());
          break;
        case MODERATOR:
          channels.addAll(chat.getModeratorChannels());
          break;
        default:
          throw new ProgrammingError("Uncovered group: " + groupAssociation.getGroup());
      }
    }

    clientService.sendChatChannels(channels, event.getPlayer());
  }
}
