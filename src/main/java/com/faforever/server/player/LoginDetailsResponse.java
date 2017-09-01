package com.faforever.server.player;

public class LoginDetailsResponse extends PlayerInformationResponse {

  public LoginDetailsResponse(PlayerInformationResponse playerInformationResponse) {
    super(
      playerInformationResponse.getUserId(),
      playerInformationResponse.getUsername(),
      playerInformationResponse.getCountry(),
      playerInformationResponse.getPlayer()
    );
  }
}
