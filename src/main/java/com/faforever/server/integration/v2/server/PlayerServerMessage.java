package com.faforever.server.integration.v2.server;

import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client containing information about a player.
 */
@Getter
@Setter
class PlayerServerMessage extends V2ServerMessage {

  private int playerId;
  private String username;
  private String country;
  private Player player;

  @Getter
  @Setter
  static class Player {
    Rating globalRating;
    Rating ladder1v1Rating;
    int numberOfGames;
    Avatar avatar;
    String clanTag;

    @Getter
    @Setter
    static class Rating {
      double mean;
      double deviation;
    }

    @Getter
    @Setter
    static class Avatar {
      String url;
      String description;
    }
  }
}
