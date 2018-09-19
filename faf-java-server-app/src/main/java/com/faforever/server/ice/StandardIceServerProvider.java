package com.faforever.server.ice;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.config.ServerProperties.Ice;
import com.faforever.server.entity.Player;
import com.faforever.server.ice.StandardIceServerProvider.StandardIceServerProviderCondition;
import com.faforever.server.ice.TwilioService.TwilioServiceCondition;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.stream.Collectors;

@Service
@Slf4j
@Conditional(StandardIceServerProviderCondition.class)
public class StandardIceServerProvider implements IceServersProvider {

  private final ServerProperties properties;

  public StandardIceServerProvider(ServerProperties properties) {
    this.properties = properties;
  }

  @Override
  public IceServerList getIceServerList(Player player) {
    Ice ice = properties.getIce();
    int ttl = ice.getTtl();
    Instant now = Instant.now();

    String userAuth = String.format("%s:%s", now.plusSeconds(ttl).getEpochSecond(), player.getLogin());

    return new IceServerList(ttl, now,
      ice.getServers().stream()
        .map(server -> new IceServer(URI.create(server.getUrl()), userAuth, createToken(userAuth, server.getSecret()), "token"))
        .collect(Collectors.toList())
    );
  }

  @SneakyThrows
  private String createToken(String userAuth, String secret) {
    byte[] token;
    try {
      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(new SecretKeySpec(Charset.forName("cp1252").encode(secret).array(), "HmacSHA1"));
      token = mac.doFinal(Charset.forName("cp1252").encode(userAuth).array());

    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      log.error("Could not build secret key", e);
      throw new RuntimeException(e);
    }
    return Base64.getEncoder().encodeToString(token);
  }

  /**
   * Loads the bean only if Twilio is not specified and secrets are set
   */
  static class StandardIceServerProviderCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
      Environment environment = context.getEnvironment();
      boolean twilioActivated = new TwilioServiceCondition().matches(context, metadata);
      boolean serverSecretsSet = !Strings.isNullOrEmpty(environment.getProperty("fafserver.ice.servers.0.secret"));
      return !twilioActivated && serverSecretsSet;
    }
  }
}
