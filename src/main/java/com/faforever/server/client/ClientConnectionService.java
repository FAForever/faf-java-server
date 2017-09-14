package com.faforever.server.client;

import com.faforever.server.entity.Player;
import com.faforever.server.integration.Protocol;
import com.faforever.server.player.PlayerService;
import com.faforever.server.stats.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of client connections.
 */
@Service
@Slf4j
public class ClientConnectionService {

  /**
   * Client connections by connection ID.
   */
  private final Map<String, ClientConnection> connections;
  private final CounterService counterService;
  private final PlayerService playerService;
  private final ApplicationEventPublisher eventPublisher;

  public ClientConnectionService(CounterService counterService, PlayerService playerService, ApplicationEventPublisher eventPublisher) {
    this.counterService = counterService;
    this.playerService = playerService;
    this.eventPublisher = eventPublisher;
    connections = new ConcurrentHashMap<>();
  }

  @PostConstruct
  public void postConstruct() {
    counterService.reset(Metrics.CLIENTS_CONNECTED_FORMAT);
  }

  /**
   * Creates and returns a new client connection with the specified ID. Since this manager is protocol and connection
   * agnostic, it's the caller's responsibility to remove connections using {@link #removeConnection(String,
   * Protocol)}.
   */
  public ClientConnection createClientConnection(String connectionId, Protocol protocol, InetAddress inetAddress) {
    synchronized (connections) {
      Assert.state(!connections.containsKey(connectionId), "A connection with ID " + connectionId + " already exists");

      connections.put(connectionId, new ClientConnection(connectionId, protocol, inetAddress));
      counterService.increment(String.format(Metrics.CLIENTS_CONNECTED_FORMAT, protocol));

      return connections.get(connectionId);
    }
  }

  public Optional<ClientConnection> getClientConnection(String connectionId) {
    synchronized (connections) {
      return Optional.ofNullable(connections.get(connectionId));
    }
  }

  public Collection<ClientConnection> getConnections() {
    synchronized (connections) {
      return Collections.unmodifiableCollection(connections.values());
    }
  }

  public void removeConnection(String connectionId, Protocol protocol) {
    synchronized (connections) {
      Optional.ofNullable(connections.remove(connectionId)).ifPresent(clientConnection -> {
        log.debug("Removing connection '{}' with protocol '{}'", connectionId, protocol);
        eventPublisher.publishEvent(new ClientDisconnectedEvent(clientConnection, clientConnection));
        counterService.decrement(String.format(Metrics.CLIENTS_CONNECTED_FORMAT, protocol));
      });
    }
  }

  /**
   * Fires a {@link CloseConnectionEvent} in order to disconnect the client of the user with the specified ID.
   */
  void disconnectClient(Authentication requester, int userId) {
    // TODO actually there should be a user service, returning a User
    Optional<Player> optional = playerService.getOnlinePlayer(userId);
    if (!optional.isPresent()) {
      log.warn("User '{}' requested disconnection of unknown user '{}'", requester, userId);
      return;
    }
    Player player = optional.get();
    eventPublisher.publishEvent(new CloseConnectionEvent(this, player.getClientConnection()));
    log.info("User '{}' closed connection of user '{}'", requester, player);
  }
}
