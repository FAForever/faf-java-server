package com.faforever.server.client;

import com.faforever.server.integration.Protocol;
import com.faforever.server.stats.Metrics;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

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
public class ClientConnectionManager {

  /**
   * Client connections by connection ID.
   */
  private final Map<String, ClientConnection> connections;
  private final GaugeService gaugeService;
  private ApplicationEventPublisher applicationEventPublisher;
  private Map<Protocol, AtomicInteger> connectionsByProtocol;

  public ClientConnectionManager(GaugeService gaugeService, ApplicationEventPublisher applicationEventPublisher) {
    this.gaugeService = gaugeService;
    this.applicationEventPublisher = applicationEventPublisher;
    connections = new ConcurrentHashMap<>();
    connectionsByProtocol = new ConcurrentHashMap<>();
  }

  /**
   * Creates a new connection with the specified ID. Since this manager is protocol and connection agnostic, it's the
   * caller's responsibility to remove connections using {@link #removeConnection(String, Protocol)}.
   */
  public ClientConnection obtainConnection(String connectionId, Protocol protocol) {
    connections.computeIfAbsent(connectionId, id -> {
      connectionsByProtocol.computeIfAbsent(protocol, p -> new AtomicInteger()).incrementAndGet();
      return new ClientConnection(id, protocol);
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
      applicationEventPublisher.publishEvent(new ClientDisconnectedEvent(clientConnection, clientConnection));
    });
    onConnectionsUpdated();
  }

  private void onConnectionsUpdated() {
    gaugeService.submit(Metrics.ACTIVE_CONNECTIONS, connections.size());
    connectionsByProtocol.forEach((protocol, counter)
      -> gaugeService.submit(String.format("%s.%s", Metrics.ACTIVE_CONNECTIONS, protocol.name()), counter.doubleValue()));
  }
}
