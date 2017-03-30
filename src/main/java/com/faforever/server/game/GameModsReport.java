package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.Data;

import java.util.List;

@Data
public class GameModsReport implements ClientMessage {
  private final List<String> modUids;
}
