package com.faforever.server.client;

import com.faforever.server.common.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest implements ClientMessage {

  private String uniqueId;
  private String jwt;
}
