package com.faforever.server.player;

import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.entity.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PlayerService {

  private final Map<Integer, Player> playersById;

  public PlayerService() {
    playersById = new ConcurrentHashMap<>();
  }

  @EventListener
  public void onPlayerLogin(PlayerLoginEvent event) {
    Player player = event.getPlayer();
    playersById.put(player.getId(), player);
  }

  @EventListener
  public void onClientDisconnect(ClientDisconnectedEvent event) {
    Optional.ofNullable(event.getClientConnection().getUserDetails())
      .ifPresent(userDetails -> {
        log.debug("Removing player '{}' who went offline", userDetails.getPlayer());
        playersById.remove(userDetails.getPlayer().getId());
      });
  }

  public Optional<Player> getPlayer(int id) {
    return Optional.ofNullable(playersById.get(id));
  }

  public Collection<Player> getPlayers() {
    return playersById.values();
  }
}
