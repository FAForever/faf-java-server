package com.faforever.server.game;

import lombok.Getter;

import java.util.List;

@Getter
public class GameLaunchMessage {

  private int gameId;
  private String mod;
  private List<String> commandLineArgs;

  public GameLaunchMessage(int gameId, String mod, List<String> commandLineArgs) {
    this.gameId = gameId;
    this.mod = mod;
    this.commandLineArgs = commandLineArgs;
  }
}
