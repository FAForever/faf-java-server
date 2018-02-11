package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Message sent from the client to the server informing it about the activated simulation mods in the game.
 */
@Getter
@AllArgsConstructor
class GameModsClientMessage extends V2ClientMessage {
  /** The UIDs of the mods that are active. */
  private List<String> uids;
}
