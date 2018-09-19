package com.faforever.server.ice;


import com.faforever.server.player.Player;

/**
 * Interface for ICE server list providers.
 */
public interface IceServersProvider {
  IceServerList getIceServerList(Player player);
}
