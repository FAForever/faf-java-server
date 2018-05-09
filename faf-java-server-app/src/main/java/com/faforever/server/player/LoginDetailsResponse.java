package com.faforever.server.player;

public class LoginDetailsResponse extends PlayerResponse {

  public LoginDetailsResponse(PlayerResponse playerResponse) {
    super(
      playerResponse.getPlayerId(),
      playerResponse.getUsername(),
      playerResponse.getCountry(),
      playerResponse.getTimeZone(),
      playerResponse.getGlobalRating(),
      playerResponse.getLadder1v1Rating(),
      playerResponse.getNumberOfGames(),
      playerResponse.getAvatar(),
      playerResponse.getClanTag()
    );
  }
}
