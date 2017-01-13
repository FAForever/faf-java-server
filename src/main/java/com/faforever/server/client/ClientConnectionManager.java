package com.faforever.server.client;

import com.faforever.server.integration.Protocol;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
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

  public ClientConnectionManager() {
    connections = new ConcurrentHashMap<>();
  }

  public ClientConnection obtainConnection(String connectionId, Protocol protocol) {
    connections.computeIfAbsent(connectionId, id -> new ClientConnection(id, protocol));
    return connections.get(connectionId);
  }

  public Collection<ClientConnection> getConnections() {
    return Collections.unmodifiableCollection(connections.values());
  }
}
