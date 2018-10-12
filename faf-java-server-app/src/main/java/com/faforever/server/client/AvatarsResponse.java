package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

import java.util.List;

/**
 * @deprecated the client should read the avatar list from the API instead
 */
@Deprecated
@Data
public class AvatarsResponse implements ServerMessage {
  private final List<Avatar> avatars;

  @Data
  public static class Avatar {
    private final String url;
    private final String description;
  }
}
