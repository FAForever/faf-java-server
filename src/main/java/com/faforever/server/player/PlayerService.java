package com.faforever.server.player;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;

import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.client.ClientService;
import com.faforever.server.entity.Player;
import com.faforever.server.security.FafUserDetails;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PlayerService {

  private final Map<Integer, Player> onlinePlayersById;
  private final ClientService clientService;

  public PlayerService(ClientService clientService) {
    this.clientService = clientService;
    onlinePlayersById = new ConcurrentHashMap<>();
  }

  @EventListener
  public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
    FafUserDetails fafUserDetails = (FafUserDetails) event.getAuthentication().getPrincipal();
    Player player = fafUserDetails.getPlayer();

    onlinePlayersById.put(player.getId(), player);
    onlinePlayersById.values().stream()
      .filter(otherPlayer -> otherPlayer != player && otherPlayer.getClientConnection() != null)
      .forEach(otherPlayer -> clientService.sendPlayerDetails(player, otherPlayer));
  }

  @EventListener
  public void onClientDisconnect(ClientDisconnectedEvent event) {
    Optional.ofNullable(event.getClientConnection().getUserDetails())
      .ifPresent(userDetails -> {
        log.debug("Removing player '{}' who went offline", userDetails.getPlayer());
        onlinePlayersById.remove(userDetails.getPlayer().getId());
      });
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
}
