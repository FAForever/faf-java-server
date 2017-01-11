package com.faforever.server.game;

import com.faforever.server.request.ClientMessage;
import lombok.Data;

@Data
public class JoinGameRequest implements ClientMessage {
  private final int id;
  private final String password;
}
