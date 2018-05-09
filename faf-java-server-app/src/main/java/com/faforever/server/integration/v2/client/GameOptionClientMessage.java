package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Message sent from the client to the server informing it about a changed game option.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientNotification
class GameOptionClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "gameOption";

  /** The game option's key as in the game code. */
  @NotNull
  private String key;

  /** The game option's value. */
  @NotNull
  private String value;
}
