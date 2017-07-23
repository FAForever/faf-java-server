package com.faforever.server.player;

import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import com.faforever.server.game.PlayerGameState;
import com.faforever.server.stats.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.ApplicationEventPublisher;
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
import java.util.stream.Stream;

@Service
@Slf4j
@MXBean
public class PlayerService {

  private final Map<Integer, Player> onlinePlayersById;
  private final ClientService clientService;
  private final CounterService counterService;
  private final ApplicationEventPublisher eventPublisher;

  public PlayerService(ClientService clientService, CounterService counterService, ApplicationEventPublisher eventPublisher) {
    this.clientService = clientService;
    this.counterService = counterService;
    this.eventPublisher = eventPublisher;
    onlinePlayersById = new ConcurrentHashMap<>();
    Stream.of(PlayerGameState.values()).forEach(state -> counterService.reset(String.format(Metrics.PLAYER_GAMES_STATE_FORMAT, state)));
  }

  public void setPlayerOnline(Player player) {
    log.debug("Adding player '{}'", player);

    onlinePlayersById.put(player.getId(), player);
    counterService.increment(String.format(Metrics.PLAYER_GAMES_STATE_FORMAT, player.getGameState()));

    List<Player> otherOnlinePlayers = getOtherOnlinePlayers(player);
    clientService.sendLoginDetails(player, player);
    clientService.sendPlayerInformation(otherOnlinePlayers, player);

    eventPublisher.publishEvent(new PlayerOnlineEvent(this, player));
    announceOnline(player);
  }

  public void removePlayer(Player player) {
    log.debug("Removing player '{}'", player);

    if (onlinePlayersById.remove(player.getId()) != null) {
      eventPublisher.publishEvent(new PlayerOfflineEvent(this, player));
    }
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
