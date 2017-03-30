package com.faforever.server.client;

import com.faforever.server.common.ServerMessage;
import com.faforever.server.ice.IceServerList;
import lombok.Data;

import java.util.List;

@Data
public class IceServersResponse implements ServerMessage {
  private final List<IceServerList> iceServerLists;
}
