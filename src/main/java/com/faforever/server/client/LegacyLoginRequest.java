package com.faforever.server.client;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @deprecated for backward compatibility only, use @{@link LoginRequest} for non-legacy protocols instead.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Deprecated
public class LegacyLoginRequest implements ClientMessage {

  private String login;
  private String password;
  private String uniqueId;
}
