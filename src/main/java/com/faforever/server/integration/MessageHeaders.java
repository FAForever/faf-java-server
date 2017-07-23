package com.faforever.server.integration;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

/**
 * Holds the names of our own message headers.
 */
public final class MessageHeaders {

  /** Boolean flag to indicate whether a message is to be broadcasted. */
  public static final String BROADCAST = "broadcast";

  /**
   * The {@link com.faforever.server.client.ClientConnection} a message belongs to. Basically, this tells us which
   * client sent the message.
   */
  public static final String CLIENT_CONNECTION = "clientConnection";

  /**
   * Contains the sender's {@link org.springframework.security.core.Authentication}. Its principal is expected to be of
   * type {@link com.faforever.server.security.FafUserDetails}.
   */
  public static final String USER_HEADER = SimpMessageHeaderAccessor.USER_HEADER;

  private MessageHeaders() {
    // Not instantiatable
  }

  public static final String WS_SESSION_ID = SimpMessageHeaderAccessor.SESSION_ID_HEADER;
}
