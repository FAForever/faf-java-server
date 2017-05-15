package com.faforever.server.client;

import com.faforever.server.game.PlayerGameState;
import com.faforever.server.integration.ChannelNames;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * <p>Notifies about a client that has been disconnected.</p> <p><strong>Caveat:</strong> This event is not meant to be
 * received via event listeners. Since there may be some pending messages in the incoming message queue that need to be
 * processed first (like a {@link PlayerGameState#ENDED}), the disconnect event needs to be put into the queue as well.
 * Therefore, instances of this event need to be send to the channel {@link ChannelNames#INBOUND_DISPATCH} and be listened for
 * by subscribing to {@link ChannelNames#CLIENT_DISCONNECTED_EVENT}.</p>
 */
@Getter
public class ClientDisconnectedEvent extends ApplicationEvent {
  private final ClientConnection clientConnection;

  public ClientDisconnectedEvent(Object source, ClientConnection clientConnection) {
    super(source);
    this.clientConnection = clientConnection;
  }
}
