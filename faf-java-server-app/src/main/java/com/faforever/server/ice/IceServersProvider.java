package com.faforever.server.ice;

import com.faforever.server.entity.Player;

/**
 * Priorities of ICE servers: 1. Twillio 2. Standard ICE server protocol 3. Custom ICE server protocol
 */
public interface IceServersProvider {
  IceServerList getIceServerList(Player player);
}
