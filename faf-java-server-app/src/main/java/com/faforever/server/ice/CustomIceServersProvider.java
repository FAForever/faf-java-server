package com.faforever.server.ice;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.config.ServerProperties.Ice;
import com.faforever.server.player.Player;
import com.google.common.annotations.VisibleForTesting;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IceServersProvider} that provides a list of ICE servers as configured in the application
 * settings.
 */
@Service
public class CustomIceServersProvider implements IceServersProvider {

  private static final String HMAC_SHA_1 = HmacAlgorithms.HMAC_SHA_1.getName();
  private final ServerProperties properties;
  private Supplier<Instant> timeProvider;

  public CustomIceServersProvider(ServerProperties properties) {
    this.properties = properties;
    timeProvider = Instant::now;
  }

  @Override
  public IceServerList getIceServerList(Player player) {
    Ice ice = properties.getIce();
    int ttl = ice.getTtl();
    Instant now = timeProvider.get();

    // Long-term credentials, see https://tools.ietf.org/html/rfc5389#section-10.2
    String userAuth = String.format("%s:%s", now.plusSeconds(ttl).getEpochSecond(), player.getLogin());

    return new IceServerList(ttl, now,
      ice.getServers().stream()
        .map(server -> new IceServer(URI.create(server.getUrl()), userAuth, createToken(userAuth, server.getSecret()), "token"))
        .collect(Collectors.toList())
    );
  }

  @SneakyThrows
  private String createToken(String userAuth, String secret) {
    Mac mac = Mac.getInstance(HMAC_SHA_1);
    mac.init(new SecretKeySpec(StandardCharsets.ISO_8859_1.encode(secret).array(), HmacAlgorithms.HMAC_SHA_1.getName()));
    byte[] token = mac.doFinal(StandardCharsets.ISO_8859_1.encode(userAuth).array());
    return Base64.getEncoder().encodeToString(token);
  }

  @VisibleForTesting
  void setTimeProvider(Supplier<Instant> timeProvider) {
    this.timeProvider = timeProvider;
  }
}
