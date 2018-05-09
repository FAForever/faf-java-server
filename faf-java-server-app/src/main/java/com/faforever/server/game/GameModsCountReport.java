package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.Value;

/**
 * Reports the number of active game mods.
 *
 * @deprecated I'm not sure what's the point of this since there is also a report that sends the list of active mod
 * UIDs. Therefore, this might get removed in future.
 */
@Value
@Deprecated
public class GameModsCountReport implements ClientMessage {
  int count;
}
