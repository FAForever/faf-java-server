package com.faforever.server.client;

import com.faforever.server.common.ServerResponse;
import com.faforever.server.ice.IceServerList;
import lombok.Data;

import java.util.List;

@Data
public class IceServersResponse implements ServerResponse {
  private final List<IceServerList> iceServerLists;
}
