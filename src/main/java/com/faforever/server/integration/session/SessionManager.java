package com.faforever.server.integration.session;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionManager {

  /**
   * Sessions by ID.
   */
  private final Map<String, Session> sessions;

  public SessionManager() {
    sessions = new ConcurrentHashMap<>();
  }

  public Session obtainSession(String sessionId) {
    sessions.computeIfAbsent(sessionId, Session::new);
    return sessions.get(sessionId);
  }
}
