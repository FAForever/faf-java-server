package com.faforever.server.avatar;

import com.faforever.server.request.ClientMessage;
import lombok.Data;

@Data
public class AddAvatarAdminRequest implements ClientMessage {
  private final int avatarId;
  private final int userId;
}
