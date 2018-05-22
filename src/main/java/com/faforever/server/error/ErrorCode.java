package com.faforever.server.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
  INVALID_LOGIN(100, "Invalid login credentials", "Please check your username and or password."),
  ALREADY_IN_GAME(101, "Already in game", "You can't join a game while you are still in another one."),
  NOT_IN_A_GAME(102, "Unexpected game message", "The server received a game message from you, however, you are currently not associated with a game. This can happen if you got disconnected from the server."),
  GAME_NOT_JOINABLE(103, "Game not joinable", "The game you tried to join has been started already and can't be joined anymore."),
  COOP_CANT_REPORT_NOT_IN_GAME(104, "Can't report Co-Op result", "Your game result could not be reported since you are not associated with a game. This may happen if you got disconnected from the server."),
  CREATE_ACCOUNT_IS_DEPRECATED(105, "Unsupported operation", "Creating accounts is no longer supported. Please visit the website http://www.faforever.com instead."),
  HOST_ONLY_OPTION(106, "Invalid operation", "The option ''{0}'' can only be set by the host of the game"),
  UID_USED_BY_MULTIPLE_USERS(107, "Login denied", "Your computer is associated with too many FAF accounts.<br>To continue using them, you have to link them to Steam: <a href=\"{0}\">{0}</a>.<br>If you need an exception to this rule, please contact an admin on the forums."),
  UID_USED_BY_ANOTHER_USER(108, "Login denied", "Your computer is already associated with another FAF account.<br><br>To log in with a new account, you have to link it to Steam: <a href=\"{0}\">{0}</a>.<br>If you need an exception to this rule, please contact an admin on the forums."),
  UNSUPPORTED_REQUEST(109, "Unsupported request", "The server received an unsupported request from your client: {0}"),
  BANNED_FROM_MATCH_MAKER(110, "Banned from match maker", "You have been banned from the match maker."),
  MATCH_MAKER_POOL_DOESNT_EXIST(111, "No such match maker pool", "Match maker pool with name ''{0}'' does not exist."),
  CANT_RESTORE_GAME_DOESNT_EXIST(112, "Can't restore game connection", "The game you were connected to does no longer exist."),
  CANT_RESTORE_GAME_NOT_PARTICIPANT(113, "Can't restore game connection", "You have never been part of this game."),
  USER_ALREADY_CONNECTED(114, "User ''{0}'' is already connected", "Please close any other client that is still connected to the server."),
  INVALID_GAME_STATE(115, "Invalid game state", "The requested operation is not allowed for games in state ''{0}'', only in ''{1}''."),
  INVALID_COMMAND(116, "Invalid command", "The command ''{0}'' is not known by the server."),
  INVALID_PLAYER_GAME_STATE_TRANSITION(117, "Invalid player game state", "Can't transition player from state ''{0}'' to ''{1}''."),
  EITHER_AVATAR_ID_OR_URL(118, "Invalid avatar command", "Either the avatar URL or the avatar ID must be set."),
  INVALID_PASSWORD(119, "Invalid password", "The password you provided is not correct."),
  NOT_A_PLAYER(120, "Not a player", "This request can only be made if the authenticated user is a player."),
  PLAYER_NOT_AVAILABLE_FOR_MATCHMAKING_OFFLINE(121, "Player unavailable for matchmaking", "The player ''{0}'' is currently not available for matchmaking as she/he is offline."),
  UNKNOWN_MAP(122, "Unknown map", "The map ''{0}'' is unknown by the server."),
  INSUFFICIENT_MATCH_PARTICIPANTS(123, "Insufficient match participants", "Can't create match with {0,number,#} participants, at least {1,number,#} are required."),
  HOST_FAILED_TO_START_GAME(124, "Host failed to start game", "The game ''{0}'' could not be started since its host ''{1}}'' failed to start his game."),
  INVALID_FEATURED_MOD(125, "Invalid featured mod", "The featured mod ''{0}'' does not exist.");

  private final int code;
  private final String title;
  private final String detail;

  ErrorCode(int code, String title, String detail) {
    this.code = code;
    this.title = title;
    this.detail = detail;
  }
}
