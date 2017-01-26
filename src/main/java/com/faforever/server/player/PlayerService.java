package com.faforever.server.player;

import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import com.faforever.server.geoip.GeoIpService;
import com.faforever.server.security.FafUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PlayerService {

  private final Map<Integer, Player> playersById;
  private final ClientService clientService;
  private final GeoIpService geoIpService;

  public PlayerService(ClientService clientService, GeoIpService geoIpService) {
    this.clientService = clientService;
    this.geoIpService = geoIpService;
    playersById = new ConcurrentHashMap<>();
  }

  @EventListener
  public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
    FafUserDetails fafUserDetails = (FafUserDetails) event.getAuthentication().getPrincipal();
    Player player = fafUserDetails.getPlayer();

    playersById.put(player.getId(), player);

    String country = geoIpService.lookupCountry(player.getClientConnection().getInetAddress());
    player.setCountry(country);

    playersById.values().forEach(otherPlayer -> clientService.sendPlayerDetails(otherPlayer, player));
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
