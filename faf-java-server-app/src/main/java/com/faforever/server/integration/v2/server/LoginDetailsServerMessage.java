package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client containing details about the logged in player.
 */
@Getter
@Setter
@V2ServerResponse
public class LoginDetailsServerMessage extends PlayerServerMessage {
  public static final String TYPE_NAME = "loginDetails";
}
