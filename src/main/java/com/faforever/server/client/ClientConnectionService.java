package com.faforever.server.client;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.Player;
import com.faforever.server.integration.Protocol;
import com.faforever.server.player.PlayerService;
import com.faforever.server.stats.Metrics;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keeps track of client connections.
 */
@Service
@Slf4j
public class ClientConnectionService {

  @VisibleForTesting
  static final String TAG_PROTOCOL = "protocol";

  /**
   * Client connections by connection ID.
   */
  private final Map<String, ClientConnection> connections;
  private final PlayerService playerService;
  private final ApplicationEventPublisher eventPublisher;
  private final Map<Protocol, AtomicInteger> protocolConnectionCounters;
  private final ServerProperties serverProperties;

  public ClientConnectionService(MeterRegistry meterRegistry, PlayerService playerService,
                                 ApplicationEventPublisher eventPublisher, ServerProperties serverProperties) {
    this.playerService = playerService;
    this.eventPublisher = eventPublisher;
    this.serverProperties = serverProperties;
    connections = new ConcurrentHashMap<>();

    Builder<Protocol, AtomicInteger> protocolConnectionCountersBuilder = ImmutableMap.builder();
    for (Protocol protocol : Protocol.values()) {
      AtomicInteger atomicInteger = new AtomicInteger();
      protocolConnectionCountersBuilder.put(protocol, atomicInteger);

      Gauge.builder(Metrics.CLIENTS, atomicInteger, AtomicInteger::get)
        .description("The number of clients that are currently connected.")
        .tag(TAG_PROTOCOL, protocol.name())
        .register(meterRegistry);
    }

    protocolConnectionCounters = protocolConnectionCountersBuilder.build();
  }

  /**
   * Creates and returns a new client connection with the specified ID. Since this manager is protocol and connection
   * agnostic, it's the caller's responsibility to remove connections using {@link #removeConnection(String,
   * Protocol)}.
   */
  public ClientConnection createClientConnection(String connectionId, Protocol protocol, InetAddress inetAddress) {
    synchronized (connections) {
      Assert.state(!connections.containsKey(connectionId), "A connection with ID " + connectionId + " already exists");

      log.debug("Registering connection '{}' from '{}' using protocol '{}'", connectionId, inetAddress, protocol);
      connections.put(connectionId, new ClientConnection(connectionId, protocol, inetAddress));
      protocolConnectionCounters.get(protocol).incrementAndGet();

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
        eventPublisher.publishEvent(new ClientDisconnectedEvent(this, clientConnection));
        protocolConnectionCounters.get(protocol).decrementAndGet();
      });
    }
  }

  public void updateLastSeen(ClientConnection connection, Instant time) {
    connection.setLastSeen(time);
  }

  @Scheduled(fixedDelay = 10_000)
  public void disconnectSilentClients() {
    Instant deadline = Instant.now().minusMillis(serverProperties.getClientConnectionTimeout());
    log.trace("Disconnecting clients that have been idle since '{}", deadline);

    connections.values().parallelStream()
      .filter(clientConnection -> clientConnection.getLastSeen().isBefore(deadline))
      .forEach(this::disconnectClient);
  }

  private void disconnectClient(ClientConnection clientConnection) {
    eventPublisher.publishEvent(new CloseConnectionEvent(this, clientConnection));
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
    log.debug("'{}' is closing connection of player '{}'", requester, player);
    disconnectClient(player.getClientConnection());
  }
}
