package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientRequest;
import com.faforever.server.game.GameVisibility;
import com.faforever.server.integration.v2.server.HostGameServerMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Message sent from the client to the server to request hosting a game.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@V2ClientRequest(successResponse = HostGameServerMessage.class)
class HostGameClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "hostGame";

  /** The name of the map to be hosted. This is the maps directory name. */
  @NotNull
  private String mapName;

  /** The game's title. Limited to 128 characters and latin1 characters by the database. */
  @NotNull
  private String title;

  /** The technical name of the "featured mod", e.g. "faf". */
  @NotNull
  private String modName;

  /** The password required to join the game. */
  private String password;
  /** Whether the game is visible to the public or restricted. */
  private GameVisibility visibility;
  /** The minimum rating required to join this game. */
  private Integer minRating;
  /** The maximum rating requried to join this game. */
  private Integer maxRating;
}
