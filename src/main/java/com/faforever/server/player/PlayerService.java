package com.faforever.server.player;

import com.faforever.server.entity.Player;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
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

  public Optional<Player> getPlayer(int id) {
    return Optional.ofNullable(playersById.get(id));
  }

  public Collection<Player> getPlayers() {
    return playersById.values();
  }
}
