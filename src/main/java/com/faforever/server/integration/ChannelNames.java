package com.faforever.server.integration;

import com.faforever.server.avatar.AvatarRequest;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.Player;
import com.faforever.server.integration.request.HostGameRequest;
import com.faforever.server.integration.request.JoinGameRequest;
import com.faforever.server.integration.request.UpdateGameStateRequest;
import com.faforever.server.integration.session.AskSessionRequest;
import com.faforever.server.request.ClientRequest;
import com.faforever.server.response.ServerResponse;
import com.faforever.server.security.LoginRequest;
import com.faforever.server.social.SocialAddRequest;
import com.faforever.server.social.SocialRemoveRequest;

public class ChannelNames {

  /**
   * Channel for {@link JoinGameRequest JoinGameRequests}
   */
  public static final String JOIN_GAME_REQUEST = "joinGameRequest";

  /**
   * Channel for {@link LoginRequest}.
   */
  public static final String LEGACY_LOGIN_REQUEST = "loginRequest";

  /**
   * Channel for outbound client messages. The payload of messages in this channel is {@link ServerResponse}.
   */
  public static final String CLIENT_OUTBOUND = "clientOutbound";

  /**
   * Channel for inbound client messages. The payload of messages in this channel is {@link ClientRequest}.
   */
  public static final String CLIENT_INBOUND = "clientInbound";

  /**
   * Channel for {@link AskSessionRequest}.
   */
  public static final String LEGACY_SESSION_REQUEST = "sessionRequest";

  /**
   * Channel for outbound legacy messages. The payload of messages in this channel is {@code Map<String, Serializable>}.
   */
  public static final String LEGACY_OUTBOUND = "legacyOutbound";

  /**
   * Channel for {@link AvatarRequest}.
   */
  public static final String LEGACY_AVATAR_REQUEST = "avatarRequest";

  /**
   * Channel for {@link SocialAddRequest}.
   */
  public static final String LEGACY_ADD_FRIEND_REQUEST = "addFriendRequest";

  /**
   * Channel for {@link SocialRemoveRequest}.
   */
  public static final String LEGACY_REMOVE_FRIEND_REQUEST = "removeFriendRequest";

  /**
   * Channel for {@link HostGameRequest}.
   */
  public static final String HOST_GAME_REQUEST = "hostGameRequest";

  /**
   * Channel for {@link UpdateGameStateRequest}.
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
   * Channel for {@link com.faforever.server.game.GameOptionRequest}.
   */
  public static final String GAME_OPTION_REQUEST = "gameOptionRequest";

  /**
   * Channel for {@link com.faforever.server.game.PlayerOptionRequest}.
   */
  public static final String PLAYER_OPTION_REQUEST = "playerOptionRequest";

  /**
   * Channel for {@link com.faforever.server.game.ClearSlotRequest}.
   */
  public static final String CLEAR_SLOT_REQUEST = "clearSlotRequest";

  /**
   * Channel for {@link com.faforever.server.game.AiOptionRequest}.
   */
  public static final String AI_OPTION_REQUEST = "aiOptionRequest";
}
