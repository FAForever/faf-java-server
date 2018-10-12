package com.faforever.server.avatar;

import com.faforever.server.common.ClientMessage;

/**
 * @deprecated the client should read the avatar list from the API instead
 */
@Deprecated
public enum ListAvatarsRequest implements ClientMessage {
  INSTANCE
}
