package com.faforever.server.integration.v2.client;

import com.faforever.server.game.GameVisibility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to request hosting a game.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
class HostGameClientMessage extends V2ClientMessage {
  /** The name of the map to be hosted. This is the maps directory name. */
  private String mapName;
  /** The game's title. Limited to 128 characters and latin1 characters by the database. */
  private String title;
  /** The technical name of the "featured mod". */
  private String modName;
  /** The password required to join the game. Can be {@code null}. */
  private String password;
  /** Whether the game is visible to the public or restricted. */
  private GameVisibility visibility;
  /** The minimum rating required to join this game. Can be {@code null}. */
  private Integer minRating;
  /** The maximum rating requried to join this game. Can be {@code null}. */
  private Integer maxRating;
}
