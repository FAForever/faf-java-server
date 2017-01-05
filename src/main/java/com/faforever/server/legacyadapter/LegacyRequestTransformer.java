package com.faforever.server.legacyadapter;

import com.faforever.server.error.ProgrammingError;
import com.faforever.server.legacyadapter.dto.ClientMessageType;
import com.faforever.server.request.AskSessionRequest;
import com.faforever.server.request.AvatarRequest;
import com.faforever.server.request.ClientRequest;
import com.faforever.server.request.GameAccess;
import com.faforever.server.request.GameVisibility;
import com.faforever.server.request.HostGameRequest;
import com.faforever.server.request.JoinGameRequest;
import com.faforever.server.request.LoginRequest;
import com.faforever.server.request.MatchmakerRequest;
import com.faforever.server.request.SocialAddRequest;
import com.faforever.server.request.SocialRemoveRequest;
import org.springframework.integration.transformer.GenericTransformer;

import java.util.Map;

/**
 * Transforms requests of the legacy protocol to internal request objects.
 */
public class LegacyRequestTransformer implements GenericTransformer<Map<String, Object>, ClientRequest> {

  @Override
  public ClientRequest transform(Map<String, Object> source) {
    ClientMessageType command = ClientMessageType.fromString((String) source.get("command"));
    switch (command) {
      case HOST_GAME:
        return new HostGameRequest(
          (String) source.get("mapId"),
          (String) source.get("title"),
          (String) source.get("mod"),
          GameAccess.fromString((String) source.get("access")),
          source.get("version") == null ? null : ((Double) source.get("version")).intValue(),
          (String) source.get("password"),
          GameVisibility.fromString((String) source.get("visibility")));
      case LIST_REPLAYS:
        throw new UnsupportedOperationException("Not supported");
      case JOIN_GAME:
        return new JoinGameRequest();
      case ASK_SESSION:
        return new AskSessionRequest();
      case SOCIAL_ADD:
        return new SocialAddRequest();
      case SOCIAL_REMOVE:
        return new SocialRemoveRequest();
      case LOGIN:
        return new LoginRequest(
          (String) source.get("login"),
          (String) source.get("password"),
          (String) source.get("unique_id"));
      case GAME_MATCH_MAKING:
        return new MatchmakerRequest();
      case AVATAR:
        return new AvatarRequest();

      default:
        throw new ProgrammingError("Uncovered command: " + command);
    }
  }
}
