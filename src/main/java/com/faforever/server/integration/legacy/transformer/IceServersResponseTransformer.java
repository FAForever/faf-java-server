package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.IceServersResponse;
import com.faforever.server.ice.IceServerList;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum IceServersResponseTransformer implements GenericTransformer<IceServersResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(IceServersResponse source) {
    List<IceServerList> iceServers = source.getIceServerLists();
    IceServerList firstList = iceServers.get(0);

    return ImmutableMap.of(
      "command", "ice_servers",
      "date_created", DateTimeFormatter.ISO_INSTANT.format(firstList.getCreatedAt()),
      "ttl", firstList.getTtlSeconds(),
      "ice_servers", source.getIceServerLists().stream()
        .flatMap(iceServerList -> firstList.getServers().stream())
        .map(iceServer -> ImmutableMap.of(
          "url", iceServer.getUrl().toASCIIString(),
          "credential", iceServer.getCredential(),
          "username", iceServer.getUsername()
        ))
        .collect(Collectors.toCollection(ArrayList::new))
    );
  }
}
