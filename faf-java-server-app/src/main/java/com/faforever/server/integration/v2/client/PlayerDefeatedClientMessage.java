package com.faforever.server.integration.v2.client;


import com.faforever.server.annotations.V2ClientNotification;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to inform it that a player has been defeated.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@V2ClientNotification
class PlayerDefeatedClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "playerDefeated";

  // TODO in future, this should contain the player ID. However, the game doesn't provide this yet.
}
