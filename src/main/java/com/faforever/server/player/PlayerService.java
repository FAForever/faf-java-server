package com.faforever.server.player;

import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.client.ClientService;
import com.faforever.server.client.CloseConnectionEvent;
import com.faforever.server.entity.Player;
import com.faforever.server.security.FafUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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

  private final Map<Integer, Player> onlinePlayersById;
  private final ClientService clientService;
  private final ApplicationEventPublisher eventPublisher;

  public PlayerService(ClientService clientService, ApplicationEventPublisher eventPublisher) {
    this.clientService = clientService;
    this.eventPublisher = eventPublisher;
    onlinePlayersById = new ConcurrentHashMap<>();
  }

  @EventListener
  public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
    FafUserDetails fafUserDetails = (FafUserDetails) event.getAuthentication().getPrincipal();
    Player player = fafUserDetails.getPlayer();

    onlinePlayersById.entrySet().stream().forEach(entry -> {
      if (entry.getValue().getLogin().equals(event.getAuthentication().getName())) {
        eventPublisher.publishEvent(new CloseConnectionEvent(this, entry.getValue().getClientConnection()));
      }
    });

    onlinePlayersById.put(player.getId(), player);
    onlinePlayersById.values().stream()
      .filter(otherPlayer -> otherPlayer != player && otherPlayer.getClientConnection() != null)
      .forEach(otherPlayer -> clientService.sendPlayerDetails(otherPlayer, player));
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
}
