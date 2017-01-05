package com.faforever.server.integration;

import com.faforever.server.request.AskSessionRequest;
import com.faforever.server.request.AvatarRequest;
import com.faforever.server.request.ClientRequest;
import com.faforever.server.request.HostGameRequest;
import com.faforever.server.request.JoinGameRequest;
import com.faforever.server.request.LoginRequest;
import com.faforever.server.request.SocialAddRequest;
import com.faforever.server.request.SocialRemoveRequest;
import com.faforever.server.response.ServerResponse;

public class ChannelNames {

  /**
   * Channel for {@link JoinGameRequest JoinGameRequests}
   */
  public static final String JOIN_GAME = "joinGameRequest";

  /**
   * Channel for {@link LoginRequest}.
   */
  public static final String LEGACY_LOGIN_REQUEST = "loginRequest";

  /**
   * Channel for outbound client messages. The payload of messages in this channel is {@link ServerResponse}.
   */
  public static final String CLIENT_OUTBOUND = "clientResponse";

  /**
   * Channel for inbound client messages. The payload of messages in this channel is {@link ClientRequest}.
   */
  public static final String CLIENT_INBOUND = "client.request";

  /**
   * Channel for {@link AskSessionRequest}.
   */
  public static final String LEGACY_SESSION_REQUEST = "sessionRequest";

  /**
   * Channel for outbound legacy messages. The payload of messages in this channel is {@code Map<String, Serializable>}.
   */
  public static final String LEGACY_OUTBOUND = "client.legacy.outbound";

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
}
