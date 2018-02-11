package com.faforever.server.integration.v2.client;

import com.faforever.server.game.Faction;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Message sent from the client to the server to inform it that the current player would like to participate in a
 * match.
 */
@Getter
@AllArgsConstructor
class SearchMatchClientMessage extends V2ClientMessage {
  /** The faction the player will be playing. */
  private Faction faction;
  /** The name of the matchmaker pool to submit this search request to. */
  private String pool;
}
