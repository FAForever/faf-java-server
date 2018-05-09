package com.faforever.server.ice;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class IceServerList {
  private final int ttlSeconds;
  private final Instant createdAt;
  private final List<IceServer> servers;
}
