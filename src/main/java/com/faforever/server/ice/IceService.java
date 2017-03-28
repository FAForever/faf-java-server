package com.faforever.server.ice;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import com.faforever.server.player.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IceService {

  private final ClientService clientService;
  private final PlayerService playerService;
  private final List<IceServersProvider> serversProviders;

  public IceService(ClientService clientService, PlayerService playerService, List<IceServersProvider> serversProviders) {
    this.clientService = clientService;
    this.playerService = playerService;
    this.serversProviders = serversProviders;
  }

  public void requestIceServers(Player player) {
    List<IceServerList> iceServerLists = serversProviders.stream()
      .map(IceServersProvider::getIceServerList)
      .collect(Collectors.toList());

    clientService.sendIceServers(iceServerLists, player);
  }

  public void forwardIceMessage(Player sender, int receiverId, Object content) {
    Optional<Player> recipient = playerService.getOnlinePlayer(receiverId);
    if (!recipient.isPresent()) {
      log.warn("Player '{}' requested to send ICE message to offline player '{}': {}", sender, recipient, content);
      return;
    }

    clientService.sendIceMessage(sender.getId(), content, recipient.get());
  }
}
