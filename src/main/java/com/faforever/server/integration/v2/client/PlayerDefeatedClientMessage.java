package com.faforever.server.integration.v2.client;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to inform it that a player has been defeated.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
class PlayerDefeatedClientMessage extends V2ClientMessage {
  // TODO in future, this should contain the player ID. However, the game doesn't provide this yet.
}
