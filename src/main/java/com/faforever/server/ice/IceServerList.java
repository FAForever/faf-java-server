package com.faforever.server.ice;

import lombok.Data;

import java.time.temporal.TemporalAccessor;
import java.util.List;

@Data
public class IceServerList {
  private final int ttlSeconds;
  private final TemporalAccessor createdAt;
  private final List<IceServer> servers;
}
