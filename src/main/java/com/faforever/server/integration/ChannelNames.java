package com.faforever.server.integration;

import com.faforever.server.avatar.AvatarMessage;
import com.faforever.server.client.SessionRequest;
import com.faforever.server.coop.CoopMissionCompletedReport;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.Player;
import com.faforever.server.game.*;
import com.faforever.server.integration.request.GameStateReport;
import com.faforever.server.integration.request.HostGameRequest;
import com.faforever.server.request.ClientMessage;
import com.faforever.server.response.ServerResponse;
import com.faforever.server.security.LoginMessage;
import com.faforever.server.social.AddFriendMessage;
import com.faforever.server.social.SocialRemoveMessage;
import com.faforever.server.statistics.ArmyStatisticsReport;

public class ChannelNames {

  /**
   * Channel for {@link JoinGameRequest JoinGameRequests}
   */
  public static final String JOIN_GAME_REQUEST = "joinGameRequest";

  /**
   * Channel for {@link LoginMessage}.
   */
  public static final String LEGACY_LOGIN_REQUEST = "loginRequest";

  /**
   * Channel for outbound client messages. The payload of messages in this channel is {@link ServerResponse}.
   */
  public static final String CLIENT_OUTBOUND = "clientOutbound";

  /**
   * Channel for inbound client messages. The payload of messages in this channel is {@link ClientMessage}.
   */
  public static final String CLIENT_INBOUND = "clientInbound";

  /**
   * Channel for {@link SessionRequest}.
   */
  public static final String LEGACY_SESSION_REQUEST = "sessionRequest";

  /**
   * Channel for outbound legacy messages. The payload of messages in this channel is {@code Map<String, Serializable>}.
   */
  public static final String LEGACY_OUTBOUND = "legacyOutbound";

  /**
   * Channel for {@link AvatarMessage}.
   */
  public static final String LEGACY_AVATAR_REQUEST = "avatarRequest";

  /**
   * Channel for {@link AddFriendMessage}.
   */
  public static final String LEGACY_ADD_FRIEND_REQUEST = "addFriendRequest";

  /**
   * Channel for {@link SocialRemoveMessage}.
   */
  public static final String LEGACY_REMOVE_FRIEND_REQUEST = "removeFriendRequest";

  /**
   * Channel for {@link HostGameRequest}.
   */
  public static final String HOST_GAME_REQUEST = "hostGameRequest";

  /**
   * Channel for {@link GameStateReport}.
   */
  public static final String UPDATE_GAME_STATE_REQUEST = "updateGameStateRequest";

  /**
   * Channel for dirty {@link Game Games} which need to be broadcasted to connected clients.
   */
  public static final String DIRTY_GAMES = "dirtyGames";

  /**
   * Channel for dirty {@link Player Players} which need to be broadcasted to connected clients.
   */
  public static final String DIRTY_PLAYERS = "dirtyPlayers";

  /**
   * Channel for {@link GameOptionReport}.
   */
  public static final String GAME_OPTION_REQUEST = "gameOptionRequest";

  /**
   * Channel for {@link PlayerOptionReport}.
   */
  public static final String PLAYER_OPTION_REQUEST = "playerOptionRequest";

  /**
   * Channel for {@link ClearSlotRequest}.
   */
  public static final String CLEAR_SLOT_REQUEST = "clearSlotRequest";

  /**
   * Channel for {@link AiOptionReport}.
   */
  public static final String AI_OPTION_REQUEST = "aiOptionRequest";

  /**
   * Channel for {@link com.faforever.server.game.DesyncReport}.
   */
  public static final String DESYNC_REPORT = "desyncReport";

  /**
   * Channel for {@link ArmyScoreReport}.
   */
  public static final String ARMY_SCORE_REPORT = "armyScoreReport";

  /**
   * Channel for {@link ArmyOutcomeReport}.
   */
  public static final String ARMY_OUTCOME_REPORT = "armyOutcomeReport";

  /**
   * Channel for {@link GameModsReport}.
   */
  public static final String GAME_MODS_REPORT = "gameModsReport";

  /**
   * Channel for {@link GameModsCountReport}.
   */
  public static final String GAME_MODS_COUNT_REPORT = "gameModsCountReport";

  /**
   * Channel for {@link CoopMissionCompletedReport}.
   */
  public static final String OPERATION_COMPLETE_REPORT = "operationCompleteReport";

  /**
   * Channel for {@link ArmyStatisticsReport}.
   */
  public static final String GAME_STATISTICS_REPORT = "gameStatisticsReport";

  /**
   * Channel for {@link EnforceRatingRequest}.
   */
  public static final String ENFORCE_RATING_REQUEST = "enforceRatingRequest";

  /**
   * Channel for Team {@link TeamKillReport}.
   */
  public static final String TEAM_KILL_REPORT = "teamKillReport";
}
