package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.IceServersResponse;
import com.faforever.server.ice.IceServer;
import com.faforever.server.ice.IceServerList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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
        .flatMap(iceServerList -> iceServerList.getServers().stream())
        .map(this::toIceServer)
        .collect(Collectors.toCollection(ArrayList::new))
    );
  }

  @NotNull
  private Map<String, String> toIceServer(IceServer iceServer) {
    Map<String, String> map = new HashMap<>();
    map.put("url", iceServer.getUrl().toASCIIString());

    if (iceServer.getCredential() != null) {
      map.put("credential", iceServer.getCredential());
      map.put("credentialType", iceServer.getCredentialType());
    }
    if (iceServer.getUsername() != null) {
      map.put("username", iceServer.getUsername());
    }
    return map;
  }
}
