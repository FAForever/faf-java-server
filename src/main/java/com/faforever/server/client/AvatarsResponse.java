package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

import java.util.List;


@Data
public class AvatarsResponse implements ServerMessage {
  private final List<Avatar> avatars;

  @Data
  public static class Avatar {
    private final String url;
    private final String description;
  }
}
