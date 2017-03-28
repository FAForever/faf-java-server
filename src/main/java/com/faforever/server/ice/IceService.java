package com.faforever.server.ice;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IceService {

  private final ClientService clientService;
  private final List<IceServersProvider> serversProviders;

  public IceService(ClientService clientService, List<IceServersProvider> serversProviders) {
    this.clientService = clientService;
    this.serversProviders = serversProviders;
  }

  public void requestIceServers(Player player) {
    List<IceServerList> iceServerLists = serversProviders.stream()
      .map(IceServersProvider::getIceServerList)
      .collect(Collectors.toList());

    clientService.sendIceServers(iceServerLists, player);
  }
}
