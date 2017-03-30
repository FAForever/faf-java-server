package com.faforever.server.ice;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.config.ServerProperties.Ice;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Provides a list of custom ICE servers that can be configured in the application settings.
 */
@Service
public class CustomIceServersProvider implements IceServersProvider {

  private final ServerProperties properties;
  private final MacSigner macSigner;
  private final ObjectMapper objectMapper;

  public CustomIceServersProvider(ServerProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    macSigner = new MacSigner(properties.getJwt().getSecret());
  }

  @Override
  public IceServerList getIceServerList() {
    Ice ice = properties.getIce();
    int ttl = ice.getTtl();

    return new IceServerList(ttl, Instant.now(),
      ice.getServers().stream()
        .map(server -> new IceServer(URI.create(server.getUrl()), UUID.randomUUID().toString(), createToken(ttl)))
        .collect(Collectors.toList())
    );
  }

  @SneakyThrows
  private String createToken(int ttl) {
    return JwtHelper.encode(objectMapper.writeValueAsString(ImmutableMap.of(
      "expiresAt", Instant.now().plusSeconds(ttl).getEpochSecond()
    )), macSigner).getEncoded();
  }
}
