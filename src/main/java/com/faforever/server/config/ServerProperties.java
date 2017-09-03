package com.faforever.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "faf-server", ignoreUnknownFields = false)
public class ServerProperties {

  private int port = 8001;
  private String version = "dev";
  private Api api = new Api();
  private TrueSkill trueSkill = new TrueSkill();
  private Uid uid = new Uid();
  private MatchMaker matchMaker = new MatchMaker();
  private Game game = new Game();
  private GeoIp geoIp = new GeoIp();
  private Shutdown shutdown = new Shutdown();
  private Chat chat = new Chat();
  private Ice ice = new Ice();
  private Jwt jwt = new Jwt();
  private Jetty jetty = new Jetty();
  private Messaging messaging = new Messaging();

  @Data
  public static class Shutdown {
    /**
     * Message to broadcast to all users when the server is going to shut down.
     */
    private String message;
  }

  @Data
  public static class GeoIp {
    private String databaseUrl = "http://geolite.maxmind.com/download/geoip/database/GeoLite2-Country.mmdb.gz";
    private Path databaseFile = Paths.get("cache/geoIp.mmdb");
  }

  @Data
  public static class Game {
    /**
     * How many seconds a game needs to run per participating player in order to be ranked.
     */
    private int rankedMinTimeMultiplicator = 60;
    /**
     * How many seconds a game may be in state {@link com.faforever.server.entity.GameState#INITIALIZING} before it's
     * being terminated.
     */
    private int staleGameTimeoutSeconds = 45;
  }

  @Data
  public static class TrueSkill {
    private double initialMean = 1500;
    private double initialStandardDeviation = 500;
    private double beta = 240;
    private double dynamicFactor = 10;
    private double drawProbability = 0.1;
  }

  @Data
  public static class MatchMaker {
    /**
     * The minimum calculated game quality two or more players have to have in order to trigger an immediate match.
     */
    private double desiredGameQuality = 0.8d;

    /**
     * The minimum calculated game quality two or more players have to have in order to be matched. Any lower than
     * this will be considered unacceptable so that the players will not be matched. This should be no larger than
     * the quality two new players with initial ratings get to ensure such players get matches.
     */
    private double acceptableGameQuality = 0.4d;

    /**
     * The transition time between requiring {@link #desiredGameQuality} to {@link #acceptableGameQuality} in seconds.
     */
    private long acceptableQualityWaitTime = Duration.ofMinutes(2).getSeconds();
  }

  @Data
  public static class Uid {
    private boolean enabled = true;
    /**
     * PKCS#8 private key without decoration and newlines. Use the following command to convert from PKCS#1:
     * <p>
     * <code>openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in pkcs1.key -out pkcs8.key</code>
     * </p>
     * Note that the key currently has to be a 224 bit key.
     */
    private String privateKey;
    private String linkToSteamUrl;
  }

  @Data
  public static class Api {
    private String baseUrl = "http://localhost:8080";
    private int maxPageSize = 10_000;
    private String clientId;
    private String clientSecret;
    private String accessTokenUri;
  }

  @Data
  public static class Chat {
    /** List of channels the client is being told to join. */
    private List<String> defaultChannels = Collections.singletonList("#aeolus");
    /** List of channels administrators are being told to join. */
    private List<String> adminChannels = Collections.singletonList("#admin");
    /** List of channels moderators are being told to join. */
    private List<String> moderatorChannels = Collections.singletonList("#moderators");
    /** Format of clan channel names. Will be formatted with the clan's acronym. */
    private String clanChannelFormat = "#%s_clan";
  }

  @Data
  public static class Ice {
    private Twilio twilio = new Twilio();
    private List<Server> servers = Collections.emptyList();
    /** TTL in seconds. */
    private int ttl = 3600;

    @Data
    public static class Twilio {
      private String accountSid;
      private String authToken;
    }

    @Data
    public static class Server {
      private String secret;
      private String url;
    }
  }

  @Data
  public static class Jwt {
    /**
     * Secret used to sign and verify JWT payload.
     */
    private String secret;
  }

  @Data
  public static class Jetty {
    private int minThreads = 1;
    private int maxThreads = 10;
    private int idleTimeoutMillis = 60000;
  }

  @Data
  public static class Messaging {
    /**
     * Size of the inbound message queue. Incoming messages will be discarded as long as the queue is full.
     */
    private int legacyAdapterInboundQueueSize = 10_000;
    /**
     * Size of the outbound message queue. Outgoing messages will be discarded as long as the queue is full.
     */
    private int legacyAdapterOutboundQueueSize = 50_000;
  }
}
