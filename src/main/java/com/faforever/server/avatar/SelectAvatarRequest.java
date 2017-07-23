package com.faforever.server.avatar;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectAvatarRequest implements ClientMessage {
  // TODO Only ID should be required, however, the legacy client sends the avatar's URL
  Integer avatarId;
  String avatarUrl;
}
