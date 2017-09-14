package com.faforever.server.avatar;

import com.faforever.server.common.ClientMessage;
import lombok.Data;

@Data
public class SelectAvatarRequest implements ClientMessage {
  private final String avatarUrl;

  public SelectAvatarRequest(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }
}
