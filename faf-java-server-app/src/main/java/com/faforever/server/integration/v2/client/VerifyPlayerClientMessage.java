package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server to verify information received from another player. If multiple players
 * report spoofed data, the server may choose to kick the player who spoofed their data.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@V2ClientNotification
class VerifyPlayerClientMessage {
  public static final String TYPE_NAME = "verifyPlayer";

  /** The ID of the player to verify. */
  private int id;
  /** The player's name to verify. */
  private String name;
  /** The player's rating "mean" to verify. */
  private float mean;
  /** The player's rating "deviation" to verify. */
  private float deviation;
  /** The player's country to verify. */
  private String country;
  /** The player's avatar URL to verify. */
  private String avatarUrl;
  /** The player's avatar description to verify. */
  private String avatarDescription;
}
