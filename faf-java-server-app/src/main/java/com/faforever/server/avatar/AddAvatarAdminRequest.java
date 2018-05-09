package com.faforever.server.avatar;

import com.faforever.server.common.ClientMessage;
import lombok.Value;

/**
 * @deprecated The client should use the API instead.
 */
@Deprecated
@Value
public class AddAvatarAdminRequest implements ClientMessage {
  int avatarId;
  int playerId;
}
