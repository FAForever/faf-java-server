package com.faforever.server.integration.request;

import com.faforever.server.request.ClientRequest;
import lombok.Data;

@Data
public class JoinGameRequest implements ClientRequest {

  private int id;
}
