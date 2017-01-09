package com.faforever.server.integration.response;

import com.faforever.server.response.ServerResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StartGameProcessResponse implements ServerResponse {

  private final String mod;
  private final int gameId;
  /**
   * @deprecated the server should never send command line arguments. They should always be generated on server-side.
   * This is currently used for {@code /numgames} which shouldn't even be reported by a peer anyway, but looked up.
   */
  @Deprecated
  private final List<String> commandLineArguments;
}
