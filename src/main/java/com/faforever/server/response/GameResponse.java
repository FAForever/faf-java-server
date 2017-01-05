package com.faforever.server.response;

import lombok.Data;

@Data
public class GameResponse extends ServerResponse {

  private int id;

  public GameResponse(int id) {
    this.id = id;
  }

}
