package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.Value;

@Value
public class GameChatMessageReport implements ClientMessage {
  String message;
}
