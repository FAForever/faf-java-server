package com.faforever.server.integration.session;

import com.faforever.server.security.FafUserDetails;
import lombok.Data;

@Data
public class Session {

  /**
   * Name for the session header attribute.
   */
  public static final String SESSION = "session";

  private final String id;
  private FafUserDetails userDetails;
}
