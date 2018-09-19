package com.faforever.server.ice;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.entity.Player;
import com.faforever.server.ice.TwilioService.TwilioServiceCondition;
import com.google.common.base.Strings;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IceServersProvider} that provides Twilio ICE servers. Bean is only loaded if Twilio
 * credentials are configured.
 */
@Service
@Slf4j
@Conditional(TwilioServiceCondition.class)
public class TwilioService implements IceServersProvider {
  private final ServerProperties properties;

  public TwilioService(ServerProperties properties) {
    this.properties = properties;
  }

  @PostConstruct
  public void postConstruct() {
    ServerProperties.Ice.Twilio twilio = properties.getIce().getTwilio();
    String accountSid = twilio.getAccountSid();
    String authToken = twilio.getAuthToken();

    if (Strings.isNullOrEmpty(accountSid) || Strings.isNullOrEmpty(authToken)) {
      throw new IllegalStateException("Twilio is not configured and therefore shouldn't even haven been loaded.");
    }

    Twilio.init(accountSid, authToken);
  }

  @Override
  public IceServerList getIceServerList(Player player) {
    Token token = Token.creator().setTtl(properties.getIce().getTtl()).create();

    return new IceServerList(
      Integer.parseInt(token.getTtl()),
      Instant.ofEpochSecond(token.getDateCreated().getMillis() / 1000),
      token.getIceServers().stream()
        .map(iceServer -> new IceServer(iceServer.getUrl(), iceServer.getUsername(), iceServer.getCredential(), "token"))
        .collect(Collectors.toList())
    );
  }

  /**
   * Loads the bean only if Twilio account SID and auth token have been specified.
   */
  static class TwilioServiceCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
      Environment environment = context.getEnvironment();
      return !Strings.isNullOrEmpty(environment.getProperty("faf-server.ice.twilio.account-sid"))
        && !Strings.isNullOrEmpty(environment.getProperty("faf-server.ice.twilio.auth-token"));
    }
  }
}
