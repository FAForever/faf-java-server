package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.TimeZone;

/**
 * Message sent from the server to the client containing information about a player.
 */
@Getter
@Setter
@V2ServerResponse
class PlayerServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "player";

  /** The player's ID, e.g. {@code 51231}. */
  private int playerId;
  /** The player's username. */
  @NotNull
  private String username;
  /** The two-letter country code of the location of the player's IP address, if available. */
  private String country;
  /**
   * The time zone ID of the location of the player's IP address, if available. Either an abbreviation such as "PST", a
   * full name such as "America/Los_Angeles", or a custom ID such as "GMT-8:00"
   */
  private TimeZone timeZone;
  /** The player's rating in the global leaderboard, if any. */
  private Rating globalRating;
  /** The player's rating in the 1v1 leaderboard, if any. */
  private Rating ladder1v1Rating;
  /** The number of games the player has played. */
  @NotNull
  private int numberOfGames;
  /** The player's avatar, if any. */
  private Avatar avatar;
  /** The player's clan tag, if any. */
  private String clanTag;
  /** The game the player is currently part of. */
  private Game game;

  /** A player's TrueSkill rating. */
  @Getter
  @Setter
  static class Rating {
    /** The TrueSkill's mu, representing the perceived skill. */
    private double mean;
    /** The TrueSkill's sigma, representing how "unconfident" the system is in the player's mu value. */
    private double deviation;
  }

  /** A player's avatar. */
  @Getter
  @Setter
  static class Avatar {
    /** The avatar's URL. */
    private String url;
    /** The avatar's description. */
    private String description;
  }

  /**
   * Limited information about the player's game. The full game info is sent separately, clients should wait for the
   * respective message to complete the information once it's available.
   */
  @Getter
  @Setter
  static class Game {
    private int id;
  }
}
