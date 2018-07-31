package com.faforever.server.integration.legacy;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum LegacyClientMessageType {
  HOST_GAME("game_host"),
  JOIN_GAME("game_join"),
  /**
   * @deprecated Poor man's session handling is deprecated even in the legacy protocol, but still used by some clients.
   * Same information could be sent in the login message instead.
   */
  @Deprecated
  ASK_SESSION("ask_session"),
  /**
   * Adds another player as friend/foe.
   */
  SOCIAL_ADD("social_add"),
  /**
   * @deprecated Removing friends/foes should be done against the API.
   */
  @Deprecated
  SOCIAL_REMOVE("social_remove"),
  LOGIN("hello"),
  GAME_MATCH_MAKING("game_matchmaking"),
  /**
   * @deprecated Avatars should be loaded from the API.
   */
  @Deprecated
  AVATAR("avatar"),
  /**
   * @deprecated the legacy client still sends this message, however it will be superseded once ICE is implemented.
   */
  @Deprecated
  INITIATE_TEST("InitiateTest"),
  /**
   * @deprecated the legacy client still sends this message, however creating accounts is already handled by the API and
   * the website now and should no longer be done by the server.
   */
  @Deprecated
  CREATE_ACCOUNT("create_account"),
  /**
   * @deprecated since this is rather a message container instead of a specific message type, it should be split into
   * separate messages.
   */
  @Deprecated
  ADMIN("admin"),
  ICE_SERVERS("ice_servers"),
  RESTORE_GAME_SESSION("restore_game_session"),
  PING("ping"),

  // Game messages
  GAME_STATE("GameState"),
  GAME_OPTION("GameOption"),
  // Wondering why this isn't just a GameState? See https://github.com/FAForever/fa/issues/2378
  GAME_ENDED("GameEnded"),
  PLAYER_OPTION("PlayerOption"),
  CLEAR_SLOT("ClearSlot"),
  DESYNC("Desync"),
  GAME_MODS("GameMods"),
  GAME_RESULT("GameResult"),
  OPERATION_COMPLETE("OperationComplete"),
  JSON_STATS("JsonStats"),
  ENFORCE_RATING("EnforceRating"),
  TEAMKILL_REPORT("TeamkillReport"),
  AI_OPTION("AIOption"),
  MUTUAL_DRAW("MutualDraw"),
  ICE_MESSAGE("IceMsg"),
  DISCONNECTED("Disconnected"),
  BOTTLENECK("Bottleneck"),
  CHAT("Chat"),
  BOTTLENECK_CLEARED("BottleneckCleared"),
  VERIFY_PLAYER("VerifyPlayer");

  private static Map<String, LegacyClientMessageType> fromString;

  static {
    fromString = new HashMap<>();
    for (LegacyClientMessageType legacyClientMessageType : values()) {
      fromString.put(legacyClientMessageType.string, legacyClientMessageType);
    }
  }

  private String string;

  LegacyClientMessageType(String string) {
    this.string = string;
  }

  @JsonValue
  public String getString() {
    return string;
  }

  public static LegacyClientMessageType fromString(String string) {
    return fromString.get(string);
  }
}
