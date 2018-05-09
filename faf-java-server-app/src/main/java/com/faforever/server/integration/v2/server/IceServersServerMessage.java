package com.faforever.server.integration.v2.server;


import com.faforever.server.annotations.V2ServerResponse;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Message sent from the server to the client informing it about available ICE server lists.
 */
@Getter
@Setter
@V2ServerResponse
class IceServersServerMessage extends V2ServerMessage {

  public static final String TYPE_NAME = "iceServers";

  /**
   * The list of ICE server lists. The reason why it's a list of lists is because one service (e.g. Twilio) can offer
   * multiple servers. When the server is using multiple services, there will be multiple lists.
   */
  List<IceServerList> iceServerLists;

  /** A list of ICE servers. */
  @Getter
  @Setter
  static class IceServerList {
    /** Time to live in seconds. */
    int ttlSeconds;
    /** The creation time of this list. */
    Instant createdAt;
    /** The list of ICE servers. */
    List<IceServer> servers;

    /** Represents an ICE server. */
    @Getter
    @Setter
    static class IceServer {
      /** The ICE server's URL. */
      URI url;
      /** The username to use to log into the ICE server. */
      String username;
      /** The credential ("password") to log into the ICE server. */
      String credential;
      /**
       * The type of the credential. Content highly depends on the service. For Twilio, this is one of "gcm", "fcm", or
       * "apn" (not PII).
       */
      String credentialType;
    }
  }
}
