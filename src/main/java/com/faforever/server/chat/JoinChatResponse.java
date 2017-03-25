package com.faforever.server.chat;

import com.faforever.server.common.ServerResponse;
import lombok.Data;

import java.util.Set;

/**
 * Tells the client to join chat channels.
 */
@Data
public class JoinChatResponse implements ServerResponse {
  private final Set<String> channels;
}
