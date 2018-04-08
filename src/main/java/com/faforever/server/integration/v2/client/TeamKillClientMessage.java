package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to inform it that someone killed a team mate.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
class TeamKillClientMessage extends V2ClientMessage {
  /** How many seconds into the game the team kill happened. */
  private int time;
  /** The ID of the player who has been killed. */
  private int victimId;
  /** The name of the player who has been killed. */
  private String victimName;
  /** The ID of the player who performed the team kill. */
  private int killerId;
  /** The name of the player who performed the team kill. */
  private String killerName;
}
