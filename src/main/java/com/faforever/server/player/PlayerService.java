package com.faforever.server.player;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.management.MXBean;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@MXBean
public class PlayerService {

  private final Map<Integer, Player> onlinePlayersById;
  private final ClientService clientService;

  public PlayerService(ClientService clientService) {
    this.clientService = clientService;
    onlinePlayersById = new ConcurrentHashMap<>();
  }

  @EventListener
  public void onPlayerOnlineEvent(PlayerOnlineEvent event) {
    Player player = event.getPlayer();

    onlinePlayersById.put(player.getId(), player);

    List<Player> otherOnlinePlayers = getOtherOnlinePlayers(player);
    clientService.sendLoginDetails(player, player);
    clientService.sendPlayerInformation(otherOnlinePlayers, player);
    announceOnline(player);
  }

  public void removePlayer(Player player) {
    log.debug("Removing player '{}'", player);
    onlinePlayersById.remove(player.getId());
  }

  public Optional<Player> getOnlinePlayer(int id) {
    return Optional.ofNullable(onlinePlayersById.get(id));
  }

  public Collection<Player> getPlayers() {
    return onlinePlayersById.values();
  }

  public boolean isPlayerOnline(String login) {
    return onlinePlayersById.values().stream().anyMatch(player -> Objects.equals(player.getLogin(), login));
  }

  /**
   * Tell all players that the specified player is now online.
   */
  private void announceOnline(Player player) {
    clientService.broadcastPlayerInformation(Collections.singletonList(player));
  }

  /**
   * Gets all online players except the specified one.
   */
  private List<Player> getOtherOnlinePlayers(Player player) {
    return onlinePlayersById.values().stream()
      .filter(otherPlayer -> !Objects.equals(otherPlayer.getId(), player.getId()) && otherPlayer.getClientConnection() != null)
      .collect(Collectors.toList());
  }
}
