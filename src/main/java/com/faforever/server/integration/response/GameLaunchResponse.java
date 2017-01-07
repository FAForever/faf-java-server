package com.faforever.server.integration.response;

import lombok.Data;

import java.util.List;

@Data
public class GameLaunchResponse {

  private final int gameId;
  private final String mod;
  private final List<String> commandLineArgs;
}
