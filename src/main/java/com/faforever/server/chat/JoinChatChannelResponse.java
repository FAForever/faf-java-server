package com.faforever.server.chat;

import com.faforever.server.common.ServerMessage;
import lombok.Data;

import java.util.Set;

/**
 * Tells the client to join chat channels.
 */
@Data
public class JoinChatChannelResponse implements ServerMessage {
  private final Set<String> channels;
}
