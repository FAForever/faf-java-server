package com.faforever.server.integration;

import com.faforever.server.avatar.AvatarMessage;
import com.faforever.server.client.ClientDisconnectedEvent;
import com.faforever.server.client.DisconnectClientRequest;
import com.faforever.server.client.ListCoopRequest;
import com.faforever.server.client.LoginMessage;
import com.faforever.server.client.SessionRequest;
import com.faforever.server.common.ClientMessage;
import com.faforever.server.common.ServerMessage;
import com.faforever.server.coop.CoopMissionCompletedReport;
import com.faforever.server.game.AiOptionReport;
import com.faforever.server.game.ArmyOutcomeReport;
import com.faforever.server.game.ArmyScoreReport;
import com.faforever.server.game.ClearSlotRequest;
import com.faforever.server.game.DisconnectPeerRequest;
import com.faforever.server.game.EnforceRatingRequest;
import com.faforever.server.game.GameModsCountReport;
import com.faforever.server.game.GameModsReport;
import com.faforever.server.game.GameOptionReport;
import com.faforever.server.game.GameStateReport;
import com.faforever.server.game.HostGameRequest;
import com.faforever.server.game.JoinGameRequest;
import com.faforever.server.game.MutuallyAgreedDrawRequest;
import com.faforever.server.game.PlayerOptionReport;
import com.faforever.server.game.TeamKillReport;
import com.faforever.server.ice.IceMessage;
import com.faforever.server.ice.IceServersRequest;
import com.faforever.server.integration.legacy.transformer.RestoreGameSessionRequest;
import com.faforever.server.matchmaker.MatchMakerCancelRequest;
import com.faforever.server.matchmaker.MatchMakerSearchRequest;
import com.faforever.server.social.AddFoeRequest;
import com.faforever.server.social.AddFriendRequest;
import com.faforever.server.social.RemoveFoeRequest;
import com.faforever.server.social.RemoveFriendRequest;
import com.faforever.server.stats.ArmyStatisticsReport;

/**
 * Holds the names of all channel. A channel's name is also the name of its bean. Channels can be configured in {@link
 * com.faforever.server.config.integration.ChannelConfiguration}.
 */
public final class ChannelNames {

  /**
   * Channel for {@link JoinGameRequest JoinGameRequests}
   */
  public static final String JOIN_GAME_REQUEST = "joinGameRequest";

  /**
   * Channel for {@link LoginMessage}.
   */
  public static final String LEGACY_LOGIN_REQUEST = "loginRequest";

  /**
   * Channel for single-recipient outbound client messages. The payload of messages in this channel is {@link
   * ServerMessage}.
   */
  public static final String CLIENT_OUTBOUND = "clientOutbound";

  /**
   * Channel for broadcast outbound client messages. The payload of messages in this channel is {@link ServerMessage}.
   */
  public static final String CLIENT_OUTBOUND_BROADCAST = "clientOutboundBroadcast";

  /**
   * Channel for inbound client messages. The payload of messages in this channel is {@link ClientMessage}.
   */
  public static final String CLIENT_INBOUND = "clientInbound";

  /**
   * Channel for {@link SessionRequest}.
   */
  public static final String LEGACY_SESSION_REQUEST = "sessionRequest";

  /**
   * Channel for outbound messages to be processed by the legacy adapter.
   */
  public static final String LEGACY_OUTBOUND = "legacyOutbound";

  /**
   * Channel for {@link AvatarMessage}.
   */
  public static final String LEGACY_AVATAR_REQUEST = "avatarRequest";

  /**
   * Channel for {@link AddFriendRequest}.
   */
  public static final String LEGACY_ADD_FRIEND_REQUEST = "addFriendRequest";

  /**
   * Channel for {@link RemoveFriendRequest}.
   */
  public static final String LEGACY_REMOVE_FRIEND_REQUEST = "removeFriendRequest";

  /**
   * Channel for {@link AddFoeRequest}.
   */
  public static final String LEGACY_ADD_FOE_REQUEST = "addFoeRequest";

  /**
   * Channel for {@link RemoveFoeRequest}.
   */
  public static final String LEGACY_REMOVE_FOE_REQUEST = "removeFoeRequest";

  /**
   * Channel for {@link HostGameRequest}.
   */
  public static final String HOST_GAME_REQUEST = "hostGameRequest";

  /**
   * Channel for {@link GameStateReport}.
   */
  public static final String UPDATE_GAME_STATE_REQUEST = "updateGameStateRequest";

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
   * Channel for {@link TeamKillReport}.
   */
  public static final String TEAM_KILL_REPORT = "teamKillReport";

  /**
   * Channel for {@link ListCoopRequest}
   */
  public static final String LEGACY_COOP_LIST = "listCoopRequest";

  /**
   * Channel for {@link MatchMakerSearchRequest}.
   */
  public static final String MATCH_MAKER_SEARCH_REQUEST = "matchMakerSearchRequest";

  /**
   * Channel for {@link MatchMakerCancelRequest}.
   */
  public static final String MATCH_MAKER_CANCEL_REQUEST = "matchMakerCancelRequest";

  /**
   * Channel for {@link DisconnectPeerRequest}
   */
  public static final String DISCONNECT_PEER_REQUEST = "disconnectPeerRequest";

  /**
   * Channel for {@link DisconnectClientRequest}.
   */
  public static final String DISCONNECT_CLIENT_REQUEST = "disconnectClientRequest";

  /**
   * Channel for {@link IceServersRequest}.
   */
  public static final String ICE_SERVERS_REQUEST = "iceServersRequest";

  /**
   * Channel for {@link IceMessage}.
   */
  public static final String ICE_MESSAGE = "iceMessage";

  /**
   * Channel for {@link RestoreGameSessionRequest}.
   */
  public static final String RESTORE_GAME_SESSION_REQUEST = "restoreGameSessionRequest";

  /**
   * Channel for {@link MutuallyAgreedDrawRequest}.
   */
  public static final String MUTUALLY_AGREED_DRAW_REQUEST = "mutuallyAgreedDrawRequest";

  /**
   * Channel for all messages that need to be processed, no matter whether they are coming from an external system or
   * from the server itself. This is required to make sure that all actions are processed in a serial order no matter
   * where they originate from.
   */
  public static final String INBOUND_DISPATCH = "inboundDispatch";

  /**
   * Channel for {@link ClientDisconnectedEvent}.
   */
  public static final String CLIENT_DISCONNECTED_EVENT = "clientDisconnectedEvent";

  /**
   * Channel for raw inbound messages as received by the legacy adapter. These are the original bytes sent by the
   * client.
   */
  public static final String LEGACY_INBOUND = "legacyInbound";
}
