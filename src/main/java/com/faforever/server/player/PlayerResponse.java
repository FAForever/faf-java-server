package com.faforever.server.player;

import com.faforever.server.common.ServerMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

import java.util.TimeZone;

@Getter
@AllArgsConstructor
@ToString
public class PlayerResponse implements ServerMessage {
  int playerId;
  String username;
  String country;
  TimeZone timeZone;
  Player player;

  @Value
  public static class Player {
    Rating globalRating;
    Rating ladder1v1Rating;
    int numberOfGames;
    Avatar avatar;
    String clanTag;

    @Value
    public static class Rating {
      double mean;
      double deviation;
    }

    @Value
    public static class Avatar {
      String url;
      String description;
    }
  }
}
