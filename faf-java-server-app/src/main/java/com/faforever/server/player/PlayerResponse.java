package com.faforever.server.player;

import com.faforever.server.common.ServerMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

import java.time.Instant;
import java.util.TimeZone;

@Getter
@AllArgsConstructor
@ToString
public class PlayerResponse implements ServerMessage {
  private int playerId;
  private String username;
  private String country;
  private TimeZone timeZone;
  private Rating globalRating;
  private Rating ladder1v1Rating;
  private int numberOfGames;
  private Avatar avatar;
  private String clanTag;
  private Instant lastActive;

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
