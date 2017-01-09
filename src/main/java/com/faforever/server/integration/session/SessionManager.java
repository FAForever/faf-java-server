package com.faforever.server.integration.session;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.integration.Protocol;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionManager {

  /**
   * Sessions by ID.
   */
  private final Map<String, ClientConnection> sessions;

  public SessionManager() {
    sessions = new ConcurrentHashMap<>();
  }

  public ClientConnection obtainSession(String sessionId, Protocol protocol) {
    sessions.computeIfAbsent(sessionId, id -> new ClientConnection(id, protocol));
    return sessions.get(sessionId);
  }
}
