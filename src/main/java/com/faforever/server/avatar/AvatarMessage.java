package com.faforever.server.avatar;

import com.faforever.server.common.ClientMessage;

/**
 * @deprecated avatars should directly be requested from the API, not from the server.
 */
@Deprecated
public class AvatarMessage implements ClientMessage {

  public static final AvatarMessage INSTANCE = new AvatarMessage();

  private AvatarMessage() {
    // Singleton instance
  }
}
