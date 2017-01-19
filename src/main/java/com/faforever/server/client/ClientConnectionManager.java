package com.faforever.server.client;

import com.faforever.server.integration.Protocol;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of client sessions.
 */
@Service
public class ClientConnectionManager {

  /**
   * Client connections by connection ID.
   */
  private final Map<String, ClientConnection> connections;
  private ApplicationEventPublisher applicationEventPublisher;

  public ClientConnectionManager(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
    connections = new ConcurrentHashMap<>();
  }

  /**
   * Creates a new connection with the specified ID. Since this manager is protocol and connection agnostic, it's the
   * caller's responsibility to remove connections using {@link #removeConnection(String, Protocol)}.
   */
  public ClientConnection obtainConnection(String connectionId, Protocol protocol) {
    connections.computeIfAbsent(connectionId, id -> new ClientConnection(id, protocol));
    return connections.get(connectionId);
  }

  public Collection<ClientConnection> getConnections() {
    return Collections.unmodifiableCollection(connections.values());
  }

  public void removeConnection(String connectionId, Protocol protocol) {
    Optional.ofNullable(connections.remove(connectionId)).ifPresent(
      clientConnection -> applicationEventPublisher.publishEvent(new ClientDisconnectedEvent(clientConnection, clientConnection)));
  }
}
