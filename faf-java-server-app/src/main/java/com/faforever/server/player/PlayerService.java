package com.faforever.server.player;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientService;
import com.faforever.server.geoip.GeoIpService;
import com.faforever.server.stats.Metrics;
import com.google.common.base.Strings;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.MXBean;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@MXBean
public class PlayerService {

  private final MeterRegistry meterRegistry;
  private final OnlinePlayerRepository onlinePlayerRepository;
  private final ClientService clientService;
  private final ApplicationEventPublisher eventPublisher;
  private final GeoIpService geoIpService;

  public static final String TAG_PLAYER_GAME_STATE = "gameState";
  private static final String TAG_PLAYER_GAME_COUNTRY = "country";

  public PlayerService(
    ClientService clientService,
    MeterRegistry meterRegistry,
    OnlinePlayerRepository onlinePlayerRepository,
    ApplicationEventPublisher eventPublisher,
    GeoIpService geoIpService
  ) {
    this.clientService = clientService;
    this.meterRegistry = meterRegistry;
    this.onlinePlayerRepository = onlinePlayerRepository;
    this.eventPublisher = eventPublisher;
    this.geoIpService = geoIpService;

    Gauge.builder(Metrics.PLAYERS, onlinePlayerRepository, CrudRepository::count)
      .description("The number of players that are currently online.")
      .tag(TAG_PLAYER_GAME_STATE, "")
      .register(meterRegistry);
  }

  @Transactional
  public void setPlayerOnline(Player player) {
    log.debug("Adding player '{}'", player);

    onlinePlayerRepository.save(player);

    ClientConnection clientConnection = player.getClientConnection();
    geoIpService.lookupCountryCode(clientConnection.getClientAddress()).ifPresent(player::setCountry);
    geoIpService.lookupTimezone(clientConnection.getClientAddress()).ifPresent(player::setTimeZone);

    List<Player> otherOnlinePlayers = getOtherOnlinePlayers(player);
    clientService.sendLoginDetails(player, player);
    clientService.sendPlayerInformation(otherOnlinePlayers, player);

    eventPublisher.publishEvent(new PlayerOnlineEvent(this, player));
    announceOnline(player);

    String country = player.getCountry();
    Gauge.builder(Metrics.PLAYERS_BY_LOCATION, onlinePlayerRepository, repo -> repo.findAllByCountry(country).size())
      .description("The number of online players in country " + country)
      .tag(TAG_PLAYER_GAME_COUNTRY, Strings.nullToEmpty(country))
      .register(meterRegistry);
  }

  public void removePlayer(Player player) {
    log.debug("Removing player '{}'", player);

    onlinePlayerRepository.findById(player.getId()).ifPresent(p -> {
      onlinePlayerRepository.delete(p);
      eventPublisher.publishEvent(new PlayerOfflineEvent(this, player));
    });
  }

  public Optional<Player> getOnlinePlayer(int id) {
    return onlinePlayerRepository.findById(id);
  }

  public Collection<Player> getPlayers() {
    return StreamSupport.stream(onlinePlayerRepository.findAll().spliterator(), false).collect(Collectors.toSet());
  }

  public boolean isPlayerOnline(String login) {
    return onlinePlayerRepository.findByLogin(login).isPresent();
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
    return StreamSupport.stream(onlinePlayerRepository.findAll().spliterator(), false)
      .filter(otherPlayer -> !Objects.equals(otherPlayer.getId(), player.getId()) && otherPlayer.getClientConnection() != null)
      .collect(Collectors.toList());
  }
}
