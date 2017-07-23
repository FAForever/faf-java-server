package com.faforever.server.integration.v2.server;


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
// TODO list of list? questionable
class IceServersServerMessage extends V2ServerMessage {
  List<IceServerList> iceServerLists;

  @Getter
  @Setter
  static class IceServerList {
    int ttlSeconds;
    Instant createdAt;
    List<IceServer> servers;

    @Getter
    @Setter
    static class IceServer {
      URI url;
      String username;
      String credential;
      String credentialType;
    }
  }
}
