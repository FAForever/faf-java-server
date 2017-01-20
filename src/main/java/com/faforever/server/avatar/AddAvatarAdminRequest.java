package com.faforever.server.avatar;

import com.faforever.server.request.ClientMessage;
import lombok.Data;

/**
 * @deprecated The client should use the API instead.
 */
@Deprecated
@Data
public class AddAvatarAdminRequest implements ClientMessage {
  private final int avatarId;
  private final int playerId;
}
