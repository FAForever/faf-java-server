package com.faforever.server.player;

import com.faforever.server.entity.Player;
import com.faforever.server.integration.session.ClientConnection;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerService {

  private final List<Player> players;
  private final Map<Integer, ClientConnection> sessionsByPlayerId;

  public PlayerService() {
    players = new ArrayList<>();
    sessionsByPlayerId = new ConcurrentHashMap<>();
  }

  @EventListener
  public void onPlayerLogin(PlayerLoginEvent event) {
    players.add(event.getPlayer());
  }
}
