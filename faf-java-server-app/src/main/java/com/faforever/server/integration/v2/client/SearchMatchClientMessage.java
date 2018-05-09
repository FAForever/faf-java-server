package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Message sent from the client to the server to inform it that the current player would like to participate in a
 * match.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientNotification
class SearchMatchClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "searchMatch";

  /** The faction the player will be playing. */
  @NotNull
  private Faction faction;

  /** The name of the matchmaker pool to submit this search request to. */
  @NotNull
  private String pool;
}
