package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Message sent from the client to the server to inform the server to stop searching for a match.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientNotification
class CancelMatchSearchClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "cancelMatchSearch";

  /** The name of the matchmaker pool to cancel the search for. */
  @NotNull
  private String pool;
}
