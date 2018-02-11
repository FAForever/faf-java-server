package com.faforever.server.integration.v2.client;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Message sent from the client to the server
 */
@Getter
@AllArgsConstructor
class IceClientMessage extends V2ClientMessage {
  private int receiverId;
  private Object content;
}
