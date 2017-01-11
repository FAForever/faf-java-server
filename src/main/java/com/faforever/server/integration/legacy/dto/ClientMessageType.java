package com.faforever.server.integration.legacy.dto;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum ClientMessageType {
  HOST_GAME("game_host"),
  LIST_REPLAYS("list"),
  JOIN_GAME("game_join"),
  ASK_SESSION("ask_session"),
  SOCIAL_ADD("social_add"),
  SOCIAL_REMOVE("social_remove"),
  LOGIN("hello"),
  GAME_MATCH_MAKING("game_matchmaking"),
  AVATAR("avatar"),
  @Deprecated
  INITIATE_TEST("InitiateTest"),

  // Game messages
  GAME_STATE("GameState"),
  GAME_OPTION("GameOption"),
  PLAYER_OPTION("PlayerOption"),
  CLEAR_SLOT("ClearSlot"),
  DESYNC("Desync"),
  GAME_MODS("GameMods"),
  GAME_RESULT("GameResult"),
  OPERATION_COMPLETE("OperationComplete"),
  JSON_STATS("JsonStats"),
  ENFORCE_RATING("EnforceRating"),
  TEAMKILL_REPORT("TeamkillReport"),
  AI_OPTION("AIOption");

  private static Map<String, ClientMessageType> fromString;

  static {
    fromString = new HashMap<>();
    for (ClientMessageType clientMessageType : values()) {
      fromString.put(clientMessageType.string, clientMessageType);
    }
  }

  private String string;

  ClientMessageType(String string) {
    this.string = string;
  }

  public static ClientMessageType fromString(String string) {
    return fromString.get(string);
  }

  @JsonValue
  public String getString() {
    return string;
  }
}
