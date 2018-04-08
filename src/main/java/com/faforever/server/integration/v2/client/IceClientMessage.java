package com.faforever.server.integration.v2.client;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message sent from the client to the server
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
class IceClientMessage extends V2ClientMessage {
  private int receiverId;
  private Object content;
}
