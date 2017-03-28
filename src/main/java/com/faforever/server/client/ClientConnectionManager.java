package com.faforever.server.client;

import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.integration.Protocol;
import com.faforever.server.player.PlayerService;
import com.faforever.server.stats.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keeps track of client sessions.
 */
@Service
@Slf4j
public class ClientConnectionManager {

  /**
   * Client connections by connection ID.
   */
  private final Map<String, ClientConnection> connections;
  private final GaugeService gaugeService;
  // TODO cut cyclic dependency. Find a good way to decouple ClientConnectionManager and PlayerService.
  private final PlayerService playerService;
  private ApplicationEventPublisher eventPublisher;
  private Map<Protocol, AtomicInteger> connectionsByProtocol;

  public ClientConnectionManager(GaugeService gaugeService, PlayerService playerService, ApplicationEventPublisher eventPublisher) {
    this.gaugeService = gaugeService;
    this.playerService = playerService;
    this.eventPublisher = eventPublisher;
    connections = new ConcurrentHashMap<>();
    connectionsByProtocol = new ConcurrentHashMap<>();
  }

  /**
   * Creates a new connection with the specified ID. Since this manager is protocol and connection agnostic, it's the
   * caller's responsibility to remove connections using {@link #removeConnection(String, Protocol)}.
   */
  public ClientConnection obtainConnection(String connectionId, Protocol protocol, InetAddress inetAddress) {
    connections.computeIfAbsent(connectionId, id -> {
      connectionsByProtocol.computeIfAbsent(protocol, p -> new AtomicInteger()).incrementAndGet();
      return new ClientConnection(id, protocol, inetAddress);
    });
    onConnectionsUpdated();
    return connections.get(connectionId);
  }

  public Collection<ClientConnection> getConnections() {
    return Collections.unmodifiableCollection(connections.values());
  }

  public void removeConnection(String connectionId, Protocol protocol) {
    Optional.ofNullable(connections.remove(connectionId)).ifPresent(clientConnection -> {
      connectionsByProtocol.computeIfAbsent(clientConnection.getProtocol(), p -> new AtomicInteger()).decrementAndGet();
      eventPublisher.publishEvent(new ClientDisconnectedEvent(clientConnection, clientConnection));
    });
    onConnectionsUpdated();
  }

  private void onConnectionsUpdated() {
    gaugeService.submit(Metrics.ACTIVE_CONNECTIONS, connections.size());
    connectionsByProtocol.forEach((protocol, counter)
      -> gaugeService.submit(String.format("%s.%s", Metrics.ACTIVE_CONNECTIONS, protocol.name()), counter.doubleValue()));
  }

  /**
   * Fires a {@link CloseConnectionEvent} in order to disconnect the client of the user with the specified ID.
   */
  void disconnectClient(User requester, int userId) {
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
