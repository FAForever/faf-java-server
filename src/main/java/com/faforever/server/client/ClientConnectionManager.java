package com.faforever.server.client;

import com.faforever.server.entity.Player;
import com.faforever.server.integration.Protocol;
import com.faforever.server.player.PlayerService;
import com.faforever.server.stats.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.metrics.GaugeService;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Keeps track of client connections.
 */
@Service
@Slf4j
public class ClientConnectionManager {

  /**
   * Client connections by connection ID.
   */
  private final Map<String, ClientConnection> connections;
  private final GaugeService gaugeService;
  private final PlayerService playerService;
  private final ApplicationEventPublisher eventPublisher;
  private final Map<Protocol, AtomicInteger> connectionsByProtocol;

  public ClientConnectionManager(GaugeService gaugeService, PlayerService playerService, ApplicationEventPublisher eventPublisher) {
    this.gaugeService = gaugeService;
    this.playerService = playerService;
    this.eventPublisher = eventPublisher;
    connections = new ConcurrentHashMap<>();
    connectionsByProtocol = new ConcurrentHashMap<>();
  }

  @PostConstruct
  public void postConstruct() {
    gaugeService.submit(Metrics.ACTIVE_CONNECTIONS, 0);
    Stream.of(Protocol.values()).forEach(protocol -> submitProtocolConnections(protocol, 0));
  }

  /**
   * Creates and returns a new client connection with the specified ID. Since this manager is protocol and connection
   * agnostic, it's the caller's responsibility to remove connections using {@link #removeConnection(String,
   * Protocol)}.
   */
  public ClientConnection createClientConnection(String connectionId, Protocol protocol, InetAddress inetAddress) {
    synchronized (connections) {
      Assert.state(!connections.containsKey(connectionId), "A connection with ID " + connectionId + " already exists");

      connectionsByProtocol.computeIfAbsent(protocol, p -> new AtomicInteger()).incrementAndGet();
      connections.put(connectionId, new ClientConnection(connectionId, protocol, inetAddress));
      onConnectionsUpdated();

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
        log.debug("Removing connection '{}' at protocol '{}'", connectionId, protocol);
        connectionsByProtocol.computeIfAbsent(clientConnection.getProtocol(), p -> new AtomicInteger()).decrementAndGet();
        eventPublisher.publishEvent(new ClientDisconnectedEvent(clientConnection, clientConnection));
      });
      onConnectionsUpdated();
    }
  }

  private void onConnectionsUpdated() {
    int numberOfConnections = connections.size();
    log.debug("Updating number of connections to: {}", numberOfConnections);
    gaugeService.submit(Metrics.ACTIVE_CONNECTIONS, numberOfConnections);
    connectionsByProtocol.forEach((protocol, counter) -> submitProtocolConnections(protocol, counter.doubleValue()));
  }

  private void submitProtocolConnections(Protocol protocol, double value) {
    gaugeService.submit(String.format("%s.%s", Metrics.ACTIVE_CONNECTIONS, protocol.name()), value);
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
