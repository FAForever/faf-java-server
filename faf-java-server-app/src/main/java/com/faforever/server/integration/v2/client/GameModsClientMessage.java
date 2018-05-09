package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Message sent from the client to the server informing it about the activated simulation mods in the game.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientNotification
class GameModsClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "gameMods";

  /** The UIDs of the mods that are active. */
  @NotNull
  private List<String> uids;
}
