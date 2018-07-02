package com.faforever.server.player;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import com.faforever.server.geoip.GeoIpService;
import com.faforever.server.stats.Metrics;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.management.MXBean;
import java.time.OffsetDateTime;
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
  private final ApplicationEventPublisher eventPublisher;
  private final GeoIpService geoIpService;

  public static final String TAG_PLAYER_GAME_STATE = "gameState";

  public PlayerService(ClientService clientService, MeterRegistry meterRegistry, ApplicationEventPublisher eventPublisher, GeoIpService geoIpService) {
    this.clientService = clientService;
    this.eventPublisher = eventPublisher;
    this.geoIpService = geoIpService;
    onlinePlayersById = new ConcurrentHashMap<>();

    Gauge.builder(Metrics.PLAYERS, onlinePlayersById, Map::size)
      .description("The number of players that are currently online.")
      .tag(TAG_PLAYER_GAME_STATE, "")
      .register(meterRegistry);
  }

  public void setPlayerOnline(Player player) {
    log.debug("Adding player '{}'", player);

    onlinePlayersById.put(player.getId(), player);

    ClientConnection clientConnection = player.getClientConnection();
    geoIpService.lookupCountryCode(clientConnection.getClientAddress()).ifPresent(player::setCountry);
    geoIpService.lookupTimezone(clientConnection.getClientAddress()).ifPresent(player::setTimeZone);

    List<Player> otherOnlinePlayers = getOtherOnlinePlayers(player);
    clientService.sendLoginDetails(player, player);
    clientService.sendPlayerInformation(otherOnlinePlayers, player);

    player.setLastActive(OffsetDateTime.now());
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
